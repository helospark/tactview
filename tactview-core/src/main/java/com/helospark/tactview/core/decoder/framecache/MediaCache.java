package com.helospark.tactview.core.decoder.framecache;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.memoryoperations.MemoryOperations;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class MediaCache {
    private static final int MAX_CACHE_CLEAN_ITERATION = 20;
    private ConcurrentHashMap<String, NavigableMap<BigDecimal, MediaHashValue>> backCache = new ConcurrentHashMap<>();
    private Set<CacheRemoveDomain> accessTimeOrderedList = Collections.newSetFromMap(new ConcurrentHashMap<CacheRemoveDomain, Boolean>());
    private MemoryManager memoryManager;
    private MessagingService messagingService;
    private ScheduledExecutorService executorService;
    private MemoryOperations memoryOperations;

    @Slf4j
    private Logger logger;

    private volatile long approximateSize = 0;
    private volatile long maximumSizeHint;

    public MediaCache(MemoryManager memoryManager, @Value("${mediacache.max.size}") Long maximumSize, MessagingService messagingService,
            @Qualifier("generalTaskScheduledService") ScheduledExecutorService executorService, MemoryOperations memoryOperations) {
        this.memoryManager = memoryManager;
        this.maximumSizeHint = maximumSize;
        this.messagingService = messagingService;
        this.executorService = executorService;
        this.memoryOperations = memoryOperations;
    }

    @PostConstruct
    public void init() {
        messagingService.register(MemoryPressureMessage.class, message -> {
            doImmediateCleanup(0);
        });

        executorService.scheduleWithFixedDelay(() -> {
            try {
                if (approximateSize >= maximumSizeHint * 0.8) {
                    doImmediateCleanup((int) (maximumSizeHint * 0.5));
                } else {
                    logger.debug("Mediacache is within size limits, fill ratio {}", (double) approximateSize / maximumSizeHint);
                }
            } catch (Exception e) {
                logger.warn("Error while cleaning mediacache", e);
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);

    }

    private void doImmediateCleanup(int size) {
        TreeSet<CacheRemoveDomain> elements = new TreeSet<>((a, b) -> a.compareTo(b));
        elements.addAll(accessTimeOrderedList);
        int iteration = 0;
        while (approximateSize >= size && iteration < MAX_CACHE_CLEAN_ITERATION) {
            ++iteration;
            if (elements.isEmpty()) {
                logger.debug("ToRemove elements is empty");
                break;
            }
            CacheRemoveDomain firstElement = elements.pollFirst();
            logger.debug("Trying to clean key=\"{}\" time={} lastAccessTime={}", firstElement.key, firstElement.time, System.currentTimeMillis() - firstElement.value.lastAccessed);
            if (firstElement != null) {
                NavigableMap<BigDecimal, MediaHashValue> line = backCache.get(firstElement.key);
                if (line != null) {
                    MediaHashValue removedElement = line.remove(firstElement.time);
                    if (removedElement != null) {
                        for (var buffer : removedElement.frames) {
                            returnBuffers(buffer.allDataFrames);
                        }
                        logger.debug("Removed buffer {} {}, current buffer size: {}", firstElement.key, removedElement, approximateSize);
                    }
                }
                accessTimeOrderedList.remove(firstElement);
            }
        }
        long newSize = recalculateBufferSize();
        if (newSize != approximateSize) {
            logger.warn("Mismatch in cache size actual={}, expected={}", newSize, approximateSize);
        }
        approximateSize = newSize;
    }

    private long recalculateBufferSize() {
        Map<String, NavigableMap<BigDecimal, MediaHashValue>> copy = new HashMap<>(backCache);

        return copy.values()
                .stream()
                .flatMap(a -> a.values().stream())
                .mapToInt(a -> calculateSize(a))
                .sum();
    }

    public void cacheMedia(String key, MediaHashValue value) {
        cacheMedia(key, value, true);
    }

    public void cacheMedia(String key, MediaHashValue value, boolean cloneValue) {

        MediaHashValue clonedValue;
        if (cloneValue) {
            clonedValue = cloneValue(value);
        } else {
            clonedValue = value;
        }
        NavigableMap<BigDecimal, MediaHashValue> cachedFrames = backCache.get(key);
        if (cachedFrames == null) {
            cachedFrames = new ConcurrentSkipListMap<>();
            backCache.put(key, cachedFrames);
        }

        BigDecimal startTime = clonedValue.frames.get(0).startTime;
        MediaHashValue previousValue = cachedFrames.put(startTime, clonedValue);
        approximateSize += calculateSize(clonedValue);

        if (previousValue != null) {
            returnBuffers(previousValue.frames.stream().flatMap(a -> a.allDataFrames.stream()).collect(Collectors.toList()));
        }

        accessTimeOrderedList.add(new CacheRemoveDomain(key, clonedValue, startTime));

        if (logger.isDebugEnabled()) {
            logger.debug("Following frames are cached {}", value.frames.stream()
                    .map(a -> System.identityHashCode(a))
                    .collect(Collectors.toList()));
        }

        logger.debug("{} added to cache, current buffer size: {}", clonedValue, approximateSize);
    }

    private int calculateSize(MediaHashValue clonedValue) {
        int cacheSize = clonedValue.frames
                .stream()
                .flatMap(a -> a.allDataFrames.stream())
                .map(a -> a.capacity())
                .mapToInt(Integer::valueOf)
                .sum();
        return cacheSize;
    }

    private MediaHashValue cloneValue(MediaHashValue value) {
        List<MediaDataFrame> copied = new ArrayList<>(value.frames.size());
        for (MediaDataFrame bufferToClone : value.frames) {
            copied.add(copyValue(bufferToClone));
        }

        return new MediaHashValue(copied, value.endTime, value.startTime);
    }

    private MediaDataFrame copyValue(MediaDataFrame bufferToClone) {
        if (bufferToClone.frame != null) {
            ByteBuffer result = requestBuffersForCloning(bufferToClone.frame);
            copyToResult(result, bufferToClone.frame);
            return new MediaDataFrame(result, bufferToClone.startTime);
        } else {
            List<ByteBuffer> frames = new ArrayList<>(bufferToClone.allDataFrames.size());
            for (var entry : bufferToClone.allDataFrames) {
                ByteBuffer result = requestBuffersForCloning(entry);
                copyToResult(result, entry);
                frames.add(result);
            }
            return new MediaDataFrame(frames, bufferToClone.startTime);
        }
    }

    private ByteBuffer requestBuffersForCloning(ByteBuffer bufferToClone) {
        int requested = bufferToClone.capacity();
        ByteBuffer result = memoryManager.requestBuffer(requested);
        return result;
    }

    private void returnBuffer(ByteBuffer buffer) {
        long size = buffer.capacity();
        memoryManager.returnBuffer(buffer);
        approximateSize -= size;
        logger.trace("Returned " + System.identityHashCode(buffer) + " with size=" + size + ", new mediacache size=" + approximateSize);
    }

    private void returnBuffers(List<ByteBuffer> buffers) {
        for (var buffer : buffers) {
            returnBuffer(buffer);
        }
    }

    public Optional<MediaDataFrame> findInCache(String key, BigDecimal time) {
        NavigableMap<BigDecimal, MediaHashValue> media = backCache.get(key);
        if (media == null) {
            logger.debug("NOT found " + key + " at frame " + time);
            return Optional.empty();
        } else {
            Optional<MediaHashValue> cacheFrame = media // todo: avoid linear search
                    .values()
                    .stream()
                    .filter(value -> time.compareTo(value.startTime) >= 0 && time.compareTo(value.endTime) < 0)
                    .findFirst();

            Optional<MediaDataFrame> result = cacheFrame
                    .map(value -> value.getFrameAt(time));

            if (logger.isTraceEnabled()) {
                logger.trace("Found in cache {} for time {}", result, time);
            }

            cacheFrame.ifPresent(value -> value.lastAccessed = System.currentTimeMillis());

            logger.debug("Found " + key + " at frame " + time + " = " + result.isPresent());

            return result;
        }
    }

    // TODO: locking
    public Optional<MediaDataFrame> findInCacheAndClone(String key, BigDecimal time) {
        Optional<MediaDataFrame> result = findInCache(key, time);

        return result.map(a -> copyValue(a));
    }

    public static class MediaHashValue implements Comparable<MediaHashValue> {
        private static final Logger LOGGER = LoggerFactory.getLogger(MediaHashValue.class);
        public List<MediaDataFrame> frames;
        public BigDecimal endTime;
        public BigDecimal startTime;
        private volatile long lastAccessed = System.currentTimeMillis();

        public MediaHashValue(List<MediaDataFrame> frames, BigDecimal endTime, BigDecimal startTime) {
            this.frames = frames;
            this.endTime = endTime;
            this.startTime = startTime;
        }

        public MediaDataFrame getFrameAt(BigDecimal time) {
            var value = this;
            BigDecimal closesDistance = null;
            MediaDataFrame result = null;
            for (int i = value.frames.size() - 1; i >= 0; --i) {
                BigDecimal distance = value.frames.get(i).startTime.subtract(time).abs();

                if (closesDistance == null || distance.compareTo(closesDistance) < 0) {
                    result = value.frames.get(i);
                    closesDistance = distance;
                }
            }

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Get closest frae time={} result={}, all={} endTime={}", time, result, this.frames, endTime);
            }
            return result;
        }

        public MediaHashValue(ByteBuffer singleFrame, BigDecimal startTime, BigDecimal endTime) {
            this.frames = List.of(new MediaDataFrame(singleFrame, startTime));
            this.endTime = endTime;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
            result = prime * result + ((frames == null) ? 0 : frames.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MediaHashValue other = (MediaHashValue) obj;
            if (endTime == null) {
                if (other.endTime != null)
                    return false;
            } else if (!endTime.equals(other.endTime))
                return false;
            if (frames == null) {
                if (other.frames != null)
                    return false;
            } else if (!frames.equals(other.frames))
                return false;
            return true;
        }

        @Override
        public int compareTo(MediaHashValue o) {
            return endTime.compareTo(o.endTime);
        }

        @Override
        public String toString() {
            return "MediaHashValue [endTime=" + endTime + ", startTime=" + startTime + ", lastAccessed=" + lastAccessed + "]";
        }

    }

    public static class MediaDataFrame {
        public ByteBuffer frame;
        public BigDecimal startTime;

        public List<ByteBuffer> allDataFrames; // for audio channels

        public MediaDataFrame(ByteBuffer frame, BigDecimal startTime) {
            this.frame = frame;
            this.startTime = startTime;

            this.allDataFrames = Collections.singletonList(frame);
        }

        public MediaDataFrame(List<ByteBuffer> frames, BigDecimal startTime) {
            this.frame = null;
            this.startTime = startTime;

            this.allDataFrames = frames;
        }

        @Override
        public String toString() {
            return "MediaDataFrame [startTime=" + startTime + "]";
        }

    }

    private void copyToResult(ByteBuffer copyTo, ByteBuffer elementToCopy) {
        memoryOperations.copyBuffer(elementToCopy, copyTo, copyTo.capacity());
    }

    public static class CacheRemoveDomain {
        private String key;
        private MediaHashValue value;
        private BigDecimal time;

        public CacheRemoveDomain(String key, MediaHashValue value, BigDecimal time) {
            this.key = key;
            this.value = value;
            this.time = time;
        }

        public int compareTo(final CacheRemoveDomain other) {
            return Long.compare(value.lastAccessed, other.value.lastAccessed);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((time == null) ? 0 : time.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheRemoveDomain other = (CacheRemoveDomain) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (time == null) {
                if (other.time != null)
                    return false;
            } else if (!time.equals(other.time))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "CacheRemoveDomain [key=" + key + ", value=" + value + ", time=" + time + "]";
        }

    }

    public void dropCaches() {
        logger.debug("Drop all media caches");

        Map<String, NavigableMap<BigDecimal, MediaHashValue>> copiedElements = new HashMap<>(backCache);
        backCache.clear();
        accessTimeOrderedList.clear();
        for (var cachedEntry : copiedElements.entrySet()) {
            for (var cachedFrameSequence : cachedEntry.getValue().entrySet()) {
                for (var frame : cachedFrameSequence.getValue().frames) {
                    returnBuffers(frame.allDataFrames);
                }
            }
        }

        approximateSize = recalculateBufferSize();
    }
}
