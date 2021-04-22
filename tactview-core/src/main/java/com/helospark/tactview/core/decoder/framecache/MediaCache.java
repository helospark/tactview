package com.helospark.tactview.core.decoder.framecache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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
    private ConcurrentHashMap<String, NavigableMap<Integer, MediaHashValue>> backCache = new ConcurrentHashMap<>();
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
                NavigableMap<Integer, MediaHashValue> line = backCache.get(firstElement.key);
                if (line != null) {
                    MediaHashValue removedElement = line.remove(firstElement.value.frameStart);
                    if (removedElement != null) {
                        for (var buffer : removedElement.frames) {
                            returnBuffer(buffer);
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
                    .map(a -> a.capacity())
                    .mapToInt(Integer::valueOf)
                    .sum();
            approximateSize += newCachedSize;
        }
        NavigableMap<Integer, MediaHashValue> cachedFrames = backCache.get(key);
        if (cachedFrames == null) {
            cachedFrames = new ConcurrentSkipListMap<>();
            backCache.put(key, cachedFrames);
        }

        MediaHashValue previousValue = cachedFrames.put(clonedValue.frameStart, clonedValue);

        if (previousValue != null) {
            previousValue.frames
                    .stream()
                    .forEach(f -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(f));
        }

        accessTimeOrderedList.add(new CacheRemoveDomain(key, clonedValue));

        if (logger.isDebugEnabled()) {
            logger.debug("Following frames are cached {}", value.frames.stream()
                    .map(a -> System.identityHashCode(a))
                    .collect(Collectors.toList()));
        }

        logger.debug("{} added to cache, current buffer size: {}", clonedValue, approximateSize);
    }

    private MediaHashValue cloneValue(MediaHashValue value) {
        List<ByteBuffer> copied = new ArrayList<>(value.frames.size());
        for (ByteBuffer bufferToClone : value.frames) {
            ByteBuffer result = requestBuffersForCloning(bufferToClone);
            copyToResult(result, bufferToClone);
            copied.add(result);
        }

        return new MediaHashValue(value.frameStart, value.endIndex, copied);
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

    public Optional<MediaHashValue> findInCache(String key, int frame) {
        NavigableMap<Integer, MediaHashValue> media = backCache.get(key);
        if (media == null) {
            logger.debug("NOT found " + key + " at frame " + frame);
            return Optional.empty();
        } else {
            Optional<MediaHashValue> result = media // todo: avoid linear search
                    .values()
                    .stream()
                    .filter(value -> frame >= value.frameStart && frame < value.endIndex)
                    .findFirst();
            result.ifPresent(value -> value.lastAccessed = System.currentTimeMillis());

            logger.debug("Found " + key + " at frame " + frame + " = " + result.isPresent());

            return result;
        }
    }

    public static class MediaHashValue implements Comparable<MediaHashValue> {
        public int frameStart;
        public int endIndex;
        public List<ByteBuffer> frames;
        private volatile long lastAccessed = System.currentTimeMillis();

        public MediaHashValue(int frameStart, int endIndex, List<ByteBuffer> frames) {
            this.frameStart = frameStart;
            this.endIndex = endIndex;
            this.frames = frames;
        }

        public MediaHashValue(ByteBuffer singleFrame) {
            this.frameStart = 0;
            this.endIndex = 0;
            this.frames = List.of(singleFrame);
        }

        @Override
        public int compareTo(MediaHashValue o) {
            return Integer.valueOf(frameStart).compareTo(o.frameStart); // may not work correctly for variable length
        }

        @Override
        public String toString() {
            return "MediaHashValue [frameStart=" + frameStart + "]";
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

        public CacheRemoveDomain(String key, MediaHashValue value) {
            this.key = key;
            this.value = value;
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
            return true;
        }

    }

    public void dropCaches() {
        logger.debug("Drop all media caches");

        Map<String, NavigableMap<Integer, MediaHashValue>> copiedElements = new HashMap<>(backCache);
        backCache.clear();
        accessTimeOrderedList.clear();
        for (var cachedEntry : copiedElements.entrySet()) {
            for (var cachedFrameSequence : cachedEntry.getValue().entrySet()) {
                for (var frame : cachedFrameSequence.getValue().frames) {
                    returnBuffer(frame);
                }
            }
        }
    }

}
