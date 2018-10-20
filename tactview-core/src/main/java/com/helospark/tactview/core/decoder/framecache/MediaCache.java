package com.helospark.tactview.core.decoder.framecache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.util.ThreadSleep;
import com.helospark.tactview.core.util.logger.Slf4j;

@Component
public class MediaCache {
    private Map<MediaHashKey, NavigableMap<Integer, MediaHashValue>> backCache = new ConcurrentHashMap<>();
    private Set<CacheRemoveDomain> toRemove = new ConcurrentSkipListSet<>();
    private MemoryManager memoryManager;

    @Slf4j
    private Logger logger;

    private volatile long approximateSize = 0;
    private volatile long maximumSizeHint;
    private volatile boolean running = true;

    public MediaCache(MemoryManager memoryManager, @Value("${mediacache.max.size}") Long maximumSize) {
        this.memoryManager = memoryManager;
        this.maximumSizeHint = maximumSize;
    }

    @PostConstruct
    public void init() {
        new Thread(() -> {
            try {
                while (running) {
                    ThreadSleep.sleep(1000);
                    if (approximateSize >= maximumSizeHint * 0.8) {
                        TreeSet<CacheRemoveDomain> elements = new TreeSet<>(toRemove);
                        while (approximateSize > maximumSizeHint * 0.5) {
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
                            }
                        }
                    } else {
                        logger.debug("Mediacache is within size limits, fill ratio {}", (double) approximateSize / maximumSizeHint);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error while cleaning mediacache", e);
            }

        }).start();

    }

    @PreDestroy
    public void destroy() {
        running = false;
    }

    public void cacheMedia(MediaHashKey key, MediaHashValue value) {
        MediaHashValue clonedValue = cloneValue(value);

        NavigableMap<Integer, MediaHashValue> cachedFrames = backCache.get(key);
        if (cachedFrames == null) {
            cachedFrames = new ConcurrentSkipListMap<>();
            backCache.put(key, cachedFrames);
        }
        cachedFrames.put(clonedValue.frameStart, clonedValue);
        toRemove.add(new CacheRemoveDomain(key, clonedValue));
        logger.debug("{} added to cache, current buffer size: {}", clonedValue, approximateSize);
    }

    private MediaHashValue cloneValue(MediaHashValue value) {
        List<ByteBuffer> copied = new ArrayList<>(value.frames.size());
        for (ByteBuffer bufferToClone : value.frames) {
            ByteBuffer result = requestBuffersForCloning(bufferToClone);
            copyToResult(result, bufferToClone);
            copied.add(result);
        }

        return new MediaHashValue(value.frameStart, copied);
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
    }

    public Optional<MediaHashValue> findInCache(MediaHashKey key, int frame) {
        NavigableMap<Integer, MediaHashValue> media = backCache.get(key);
        if (media == null) {
            return Optional.empty();
        } else {
            Optional<MediaHashValue> result = media // todo: avoid linear search
                    .values()
                    .stream()
                    .filter(value -> frame >= value.frameStart && frame < value.frameStart + value.frames.size())
                    .findFirst();
            result.ifPresent(value -> value.lastAccessed = System.currentTimeMillis());
            return result;
        }
    }

    public static class MediaHashValue implements Comparable<MediaHashValue> {
        public int frameStart;
        public List<ByteBuffer> frames;
        private volatile long lastAccessed = System.currentTimeMillis();

        public MediaHashValue(int frameStart, List<ByteBuffer> frames) {
            this.frameStart = frameStart;
            this.frames = frames;
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
        copyTo.position(0);
        elementToCopy.position(0);
        copyTo.put(elementToCopy);
    }

    public static class MediaHashKey {
        public String file;
        public int width;
        public int height;

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof MediaHashKey)) {
                return false;
            }
            MediaHashKey castOther = (MediaHashKey) other;
            return Objects.equals(file, castOther.file) && Objects.equals(width, castOther.width) && Objects.equals(height, castOther.height);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, width, height);
        }

        public MediaHashKey(String file, int width, int height) {
            this.file = file;
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return "MediaHashKey [file=" + file + ", width=" + width + ", height=" + height + "]";
        }

    }

    public static class CacheRemoveDomain implements Comparable<CacheRemoveDomain> {
        private MediaHashKey key;
        private MediaHashValue value;

        public CacheRemoveDomain(MediaHashKey key, MediaHashValue value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(final CacheRemoveDomain other) {
            return Long.compare(value.lastAccessed, other.value.lastAccessed);
        }

    }

}
