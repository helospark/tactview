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
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class MediaCache {
    private ConcurrentHashMap<String, NavigableMap<BigDecimal, MediaHashValue>> backCache = new ConcurrentHashMap<>();
    private Set<CacheRemoveDomain> accessTimeOrderedList = new ConcurrentSkipListSet<>();
    private MemoryManager memoryManager;
    private MessagingService messagingService;
    private ScheduledExecutorService executorService;

    @Slf4j
    private Logger logger;

    private volatile long approximateSize = 0;
    private volatile long maximumSizeHint;

    public MediaCache(MemoryManager memoryManager, @Value("${mediacache.max.size}") Long maximumSize, MessagingService messagingService,
            @Qualifier("generalTaskScheduledService") ScheduledExecutorService executorService) {
        this.memoryManager = memoryManager;
        this.maximumSizeHint = maximumSize;
        this.messagingService = messagingService;
        this.executorService = executorService;
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
        TreeSet<CacheRemoveDomain> elements = new TreeSet<>(accessTimeOrderedList);
        while (approximateSize >= size) {
            if (elements.isEmpty()) {
                logger.debug("ToRemove elements is empty");
                break;
            }
            CacheRemoveDomain firstElement = elements.pollFirst();
            //                            logger.debug("Trying to clean {}", firstElement);
            if (firstElement != null) {
                NavigableMap<BigDecimal, MediaHashValue> line = backCache.get(firstElement.key);
                if (line != null) {
                    MediaHashValue removedElement = line.remove(firstElement.time);
                    if (removedElement != null) {
                        for (var buffer : removedElement.frames) {
                            returnBuffers(buffer.allAudioDataFrames);
                        }
                        logger.debug("Removed buffer {} {}, current buffer size: {}", firstElement.key, removedElement, approximateSize);
                    }
                }
                accessTimeOrderedList.remove(firstElement);
            }
        }
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
            int newCachedSize = value.frames
                    .stream()
                    .flatMap(a -> a.allAudioDataFrames.stream())
                    .map(a -> a.capacity())
                    .mapToInt(Integer::valueOf)
                    .sum();
            approximateSize += newCachedSize;
        }
        NavigableMap<BigDecimal, MediaHashValue> cachedFrames = backCache.get(key);
        if (cachedFrames == null) {
            cachedFrames = new ConcurrentSkipListMap<>();
            backCache.put(key, cachedFrames);
        }

        BigDecimal startTime = clonedValue.frames.get(0).startTime;
        MediaHashValue previousValue = cachedFrames.put(startTime, clonedValue);

        if (previousValue != null) {
            previousValue.frames
                    .stream()
                    .forEach(f -> GlobalMemoryManagerAccessor.memoryManager.returnBuffers(f.allAudioDataFrames));
        }

        accessTimeOrderedList.add(new CacheRemoveDomain(key, clonedValue, startTime));

        if (logger.isDebugEnabled()) {
            logger.debug("Following frames are cached {}", value.frames.stream()
                    .map(a -> System.identityHashCode(a))
                    .collect(Collectors.toList()));
        }

        logger.debug("{} added to cache, current buffer size: {}", clonedValue, approximateSize);
    }

    private MediaHashValue cloneValue(MediaHashValue value) {
        List<ByteBuffer> copied = new ArrayList<>(value.frames.size());
        for (MediaDataFrame bufferToClone : value.frames) {
            ByteBuffer result = requestBuffersForCloning(bufferToClone.frame);
            copyToResult(result, bufferToClone.frame);
            copied.add(result);
        }

        return new MediaHashValue(value.frames, value.endTime);
    }

    private ByteBuffer requestBuffersForCloning(ByteBuffer bufferToClone) {
        int requested = bufferToClone.capacity();
        ByteBuffer result = memoryManager.requestBuffer(requested);
        approximateSize += requested;
        return result;
    }

    private void returnBuffer(ByteBuffer buffer) {
        memoryManager.returnBuffer(buffer);
        approximateSize -= buffer.capacity();
        logger.debug("Returned " + System.identityHashCode(buffer) + " new size: " + approximateSize);
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
                    .filter(value -> time.compareTo(value.frames.get(0).startTime) >= 0 && time.compareTo(value.endTime) < 0)
                    .findFirst();

            Optional<MediaDataFrame> result = cacheFrame
                    .map(value -> value.getFrameAt(time));

            System.out.println("Found in cache " + result + " for time " + time);

            cacheFrame.ifPresent(value -> value.lastAccessed = System.currentTimeMillis());

            logger.debug("Found " + key + " at frame " + time + " = " + result.isPresent());

            return result;
        }
    }

    public static class MediaHashValue implements Comparable<MediaHashValue> {
        public List<MediaDataFrame> frames;
        public BigDecimal endTime;
        private volatile long lastAccessed = System.currentTimeMillis();

        public MediaHashValue(List<MediaDataFrame> frames, BigDecimal endTime) {
            this.frames = frames;
            this.endTime = endTime;
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
            if (value.frames.size() > 1) {
                System.out.println("$$$$$$$$$$$ " + time + " " + result + " all=" + this.frames + " endTime=" + endTime);
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
            return "MediaHashValue [frames=" + frames + ", endTime=" + endTime + "]";
        }

    }

    public static class MediaDataFrame {
        public ByteBuffer frame;
        public BigDecimal startTime;

        public List<ByteBuffer> allAudioDataFrames; // for audio channels 

        public MediaDataFrame(ByteBuffer frame, BigDecimal startTime) {
            this.frame = frame;
            this.startTime = startTime;

            this.allAudioDataFrames = Collections.singletonList(frame);
        }

        public MediaDataFrame(List<ByteBuffer> frame, BigDecimal startTime) {
            this.frame = null;
            this.startTime = startTime;

            this.allAudioDataFrames = frame;
        }

        @Override
        public String toString() {
            return "MediaDataFrame [startTime=" + startTime + "]";
        }

    }

    private void copyToResult(ByteBuffer copyTo, ByteBuffer elementToCopy) {
        for (int i = 0; i < elementToCopy.capacity(); ++i) {
            copyTo.put(i, elementToCopy.get(i));
        }
    }

    public static class CacheRemoveDomain implements Comparable<CacheRemoveDomain> {
        private String key;
        private MediaHashValue value;
        private BigDecimal time;

        public CacheRemoveDomain(String key, MediaHashValue value, BigDecimal time) {
            this.key = key;
            this.value = value;
            this.time = time;
        }

        @Override
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

    }

    public void dropCaches() {
        logger.debug("Drop all media caches");

        Map<String, NavigableMap<BigDecimal, MediaHashValue>> copiedElements = new HashMap<>(backCache);
        backCache.clear();
        accessTimeOrderedList.clear();
        for (var cachedEntry : copiedElements.entrySet()) {
            for (var cachedFrameSequence : cachedEntry.getValue().entrySet()) {
                for (var frame : cachedFrameSequence.getValue().frames) {
                    returnBuffers(frame.allAudioDataFrames);
                }
            }
        }
    }

}
