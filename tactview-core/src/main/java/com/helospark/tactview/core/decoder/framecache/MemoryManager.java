package com.helospark.tactview.core.decoder.framecache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.logger.Slf4j;

@Component
public class MemoryManager {
    private static final boolean DEBUG = true;
    private static final byte[] EMPTY_PIXEL = new byte[] { 0, 0, 0, 0 };
    private Map<Integer, Queue<ByteBuffer>> freeBuffersMap = new ConcurrentHashMap<>();
    private Map<ByteBuffer, String> debugTrace = Collections.synchronizedMap(new IdentityHashMap<>());
    @Slf4j
    private Logger logger;

    @PreDestroy
    public void destroy() {
        if (DEBUG) {
            for (var entry : debugTrace.entrySet()) {
                logger.error("Buffer {} never removed, allocated by {}", entry.getKey().capacity(), entry.getValue());
            }
        }
    }

    public ByteBuffer requestBuffer(Integer bytes) {
        return requestBuffers(bytes, 1).get(0);
    }

    public List<ByteBuffer> requestBuffers(Integer bytes, int count) {
        List<ByteBuffer> result = new ArrayList<>(count);
        Queue<ByteBuffer> freeBuffers = freeBuffersMap.get(bytes);
        if (freeBuffers == null) {
            freeBuffers = new ConcurrentLinkedQueue<>();
            freeBuffersMap.put(bytes, freeBuffers);
        }
        ByteBuffer element;
        while (result.size() < count && (element = freeBuffers.poll()) != null) {
            result.add(element);
        }
        logger.debug("{} {} buffer requested, from this {} was server from free buffer map, rest are newly allocated", count, bytes, result.size());
        int remainingElements = count - result.size();
        for (int i = 0; i < remainingElements; ++i) {
            result.add(ByteBuffer.allocateDirect(bytes));
        }

        if (DEBUG) {
            for (ByteBuffer a : result) {
                debugTrace.put(a, Arrays.toString(Thread.currentThread().getStackTrace()));
            }
        }

        return result;
    }

    public void returnBuffer(ByteBuffer buffer) {
        returnBuffers(Collections.singletonList(buffer));
    }

    public void returnBuffers(List<ByteBuffer> buffers) {
        for (ByteBuffer buffer : buffers) {
            clearBuffer(buffer);
            freeBuffersMap.compute(buffer.capacity(), (k, value) -> {
                if (value == null) {
                    value = new ConcurrentLinkedQueue<>();
                }
                value.offer(buffer);
                if (DEBUG) {
                    debugTrace.remove(buffer);
                }
                logger.debug("{} returned", buffer.capacity());
                return value;
            });
        }
    }

    private void clearBuffer(ByteBuffer buffer) {
        // TODO: more efficiency can be gained here
        buffer.position(0);
        for (int i = 0; i < buffer.capacity(); i += 4) {
            buffer.put(EMPTY_PIXEL);
        }
    }

}
