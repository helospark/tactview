package com.helospark.tactview.core.decoder.framecache;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.util.DebugImageRenderer;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;

import sun.misc.Unsafe;

@Component
public class MemoryManagerImpl implements MemoryManager {
    private Map<Integer, BufferInformation> freeBuffersMap = new ConcurrentHashMap<>();
    private Map<ByteBuffer, String> debugTrace = Collections.synchronizedMap(new IdentityHashMap<>());
    private Long maximumSizeHint;
    @Slf4j
    private Logger logger;
    private boolean debug;

    private MessagingService messagingService;
    private ScheduledExecutorService executorService;

    private Unsafe unsafe;

    private AtomicLong currentSize = new AtomicLong(0);

    private boolean firstOutOfMemoryError = true;

    public MemoryManagerImpl(@Value("${memory.manager.size}") Long maximumSizeHint, @Value("${memory.manager.debug}") boolean debug, MessagingService messagingService,
            @Qualifier("generalTaskScheduledService") ScheduledExecutorService executorService) {
        this.maximumSizeHint = maximumSizeHint;
        this.unsafe = getUnsafe();
        this.debug = debug;
        this.messagingService = messagingService;
        this.executorService = executorService;
    }

    @PostConstruct
    public void init() {
        executorService.scheduleWithFixedDelay(() -> {
            try {
                cleanupOfOldBuffers(120000L);

                if (currentSize.get() >= maximumSizeHint * 0.8) {
                    doForcefulCleanup(maximumSizeHint * 0.6);
                }
            } catch (Exception e) {
                logger.error("Error while cleaning buffer", e);
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void doForcefulCleanup(double target) {
        // step1 clean buffers not accessed in the last 2 secs
        if (currentSize.get() > target) {
            cleanupOfOldBuffers(10000);
        }
        // step2 clean 50% of buffers that have not needed an increase in size in 10 secs
        if (currentSize.get() > target) {
            clearPartOfBuffersThatNotNeededAnUpdate(10000L, 0.5);
        }
        // step3 clean 70% of buffers that have not needed an increase in size in 2 secs
        if (currentSize.get() > target) {
            clearPartOfBuffersThatNotNeededAnUpdate(2000L, 0.7);
        }
        // step4: profit???
    }

    private void clearPartOfBuffersThatNotNeededAnUpdate(long time, double percent) {
        long now = System.currentTimeMillis();
        freeBuffersMap.values()
                .stream()
                .filter(a -> now - a.lastNeededAnUpdate > time)
                .forEach(a -> {
                    int approximateSize = a.buffers.size();
                    int i = 0;
                    ByteBuffer buffer;
                    while (i <= approximateSize * percent && (buffer = a.buffers.poll()) != null) {
                        removeBuffer(buffer);
                    }
                });
    }

    private void cleanupOfOldBuffers(long oldThreashold) {
        long now = System.currentTimeMillis();
        freeBuffersMap.values()
                .stream()
                .filter(e -> now - e.lastRequestedAccessed > oldThreashold)
                .forEach(e -> {
                    ByteBuffer buffer;

                    int counter = 0;
                    while (counter < 10 && (buffer = e.buffers.poll()) != null) {
                        removeBuffer(buffer);
                        ++counter;
                    }
                });
    }

    private void removeBuffer(ByteBuffer buffer) {
        int capacity = buffer.capacity();
        currentSize.addAndGet(-capacity);
        logger.debug("Buffer removed with size {} current size {}", capacity, currentSize.get());
        try {
            unsafe.invokeCleaner(buffer);
        } catch (Throwable t) {
            logger.warn("Unable to clean bytebuffer", t);
            // It's OK, GC will take care of it, when it runs
        }
    }

    private Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @PreDestroy
    public void destroy() {
        if (debug) {
            for (var entry : debugTrace.entrySet()) {
                logger.warn("Buffer {} never removed, allocated at {}", entry.getKey().capacity(), entry.getValue());
            }
        }
    }

    @Override
    public ByteBuffer requestBuffer(Integer bytes) {
        return requestBuffers(bytes, 1).get(0);
    }

    @Override
    public List<ByteBuffer> requestBuffers(Integer bytes, int count) {
        List<ByteBuffer> result = new ArrayList<>(count);
        BufferInformation freeBuffers = freeBuffersMap.get(bytes);
        if (freeBuffers == null) {
            freeBuffers = new BufferInformation(new ConcurrentLinkedQueue<>());
            freeBuffersMap.put(bytes, freeBuffers);
        }
        freeBuffers.lastRequestedAccessed = System.currentTimeMillis();
        ByteBuffer element;
        while (result.size() < count && (element = freeBuffers.buffers.poll()) != null) {
            result.add(element);
        }
        int remainingElements = count - result.size();
        if (remainingElements > 0) {
            freeBuffers.lastNeededAnUpdate = System.currentTimeMillis();
        }
        for (int i = 0; i < remainingElements; ++i) {
            currentSize.addAndGet(bytes);
            ByteBuffer resultBuffer;
            try {
                resultBuffer = ByteBuffer.allocateDirect(bytes);
            } catch (OutOfMemoryError e) {
                logger.warn("No more memory left, trying to free some", e);
                messagingService.sendMessage(new MemoryPressureMessage());
                doForcefulCleanup(maximumSizeHint * 0.3);
                try {
                    resultBuffer = ByteBuffer.allocateDirect(bytes);
                } catch (OutOfMemoryError e2) {
                    if (firstOutOfMemoryError) {
                        DebugImageRenderer.writeToString(debugTrace);
                        firstOutOfMemoryError = false;
                    }
                    throw e2;
                }
            }
            resultBuffer.order(ByteOrder.nativeOrder());
            result.add(resultBuffer);
            logger.debug("{} {} buffer requested, from this {} was server from free buffer map, rest are newly allocated", count, bytes, result.size());
        }

        if (debug) {
            for (ByteBuffer a : result) {
                debugTrace.put(a, Arrays.toString(Thread.currentThread().getStackTrace()));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Requested buffers are: {}", bufferDebugString(result));
        }

        return result;
    }

    private List<Integer> bufferDebugString(List<ByteBuffer> result) {
        return result.stream()
                .map(a -> System.identityHashCode(a))
                .collect(Collectors.toList());
    }

    @Override
    public void returnBuffer(ByteBuffer buffer) {
        returnBuffers(Collections.singletonList(buffer));
    }

    @Override
    public void returnBuffers(List<ByteBuffer> buffers) {
        for (ByteBuffer buffer : buffers) {
            clearBuffer(buffer);
            freeBuffersMap.compute(buffer.capacity(), (k, value) -> {
                if (value == null) {
                    value = new BufferInformation(new ConcurrentLinkedQueue<>());
                }
                value.buffers.offer(buffer);
                if (debug) {
                    debugTrace.remove(buffer);
                }
                logger.debug("id={} {} returned", System.identityHashCode(buffer), buffer.capacity());
                return value;
            });
        }
    }

    @Override
    public void dropAllBuffers() {
        freeBuffersMap.values()
                .stream()
                .forEach(e -> {
                    ByteBuffer buffer;

                    while ((buffer = e.buffers.poll()) != null) {
                        removeBuffer(buffer);
                    }
                });
    }

    private void clearBuffer(ByteBuffer buffer) {
        for (int i = 0; i < buffer.capacity(); ++i) {
            buffer.put(i, (byte) 0);
        }
    }

    static class BufferInformation implements Comparable<BufferInformation> {
        public Queue<ByteBuffer> buffers;
        public volatile long lastRequestedAccessed = System.currentTimeMillis();
        public volatile long lastNeededAnUpdate = System.currentTimeMillis();

        public BufferInformation(Queue<ByteBuffer> buffers) {
            this.buffers = buffers;
        }

        @Override
        public int compareTo(BufferInformation o) {
            return Long.compare(lastNeededAnUpdate, o.lastNeededAnUpdate);
        }

        @Override
        public String toString() {
            return "BufferInformation [buffers=" + buffers + ", lastRequestedAccessed=" + lastRequestedAccessed + ", lastNeededAnUpdate=" + lastNeededAnUpdate + "]";
        }

    }

}
