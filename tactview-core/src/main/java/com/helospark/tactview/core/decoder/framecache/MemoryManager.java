package com.helospark.tactview.core.decoder.framecache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.helospark.lightdi.annotation.Component;

@Component
public class MemoryManager {
    private static final byte[] EMPTY_PIXEL = new byte[] { 0, 0, 0, 0 };
    private Map<Integer, Queue<ByteBuffer>> freeBuffersMap = new ConcurrentHashMap<>();

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
        int remainingElements = count - result.size();
        for (int i = 0; i < remainingElements; ++i) {
            result.add(ByteBuffer.allocateDirect(bytes));
        }
        return result;
    }

    public void returnBuffer(ByteBuffer buffer) {
        returnBuffers(Collections.singletonList(buffer));
    }

    public void returnBuffers(List<ByteBuffer> buffers) {
        //        int size = buffers.isEmpty() ? 0 : buffers.get(0).capacity();
        //        Queue<ByteBuffer> freeBuffers = freeBuffersMap.get(size);
        //        if (freeBuffers == null) {
        //            throw new IllegalArgumentException("This buffer cannot be returned to queue");
        //        }
        for (ByteBuffer buffer : buffers) {
            clearBuffer(buffer);
            freeBuffersMap.compute(buffer.capacity(), (k, value) -> {
                if (value == null) {
                    value = new ConcurrentLinkedQueue<>();
                }
                value.offer(buffer);
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
