package com.helospark.tactview.core.util.memoryoperations;

import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;

@Component
public class MemoryOperations {
    private NativeMemoryOperations nativeImplementation;

    public MemoryOperations(NativeMemoryOperations nativeImplementation) {
        this.nativeImplementation = nativeImplementation;
    }

    /**
     * Copies the from->to with given size.
     * Little faster than using a for loop to copy from->to.
     * I measured 10ms for 1920x1080 frame copy with the for loop approach and 1-2ms via this.
     * @param from to copy
     * @param to where the result will be. Have to be at least size length
     * @param size to copy
     */
    public void copyBuffer(ByteBuffer from, ByteBuffer to, int size) {
        CopyMemoryRequest request = new CopyMemoryRequest();
        request.from = from;
        request.to = to;
        request.size = size;

        nativeImplementation.copyBuffer(request);
    }

    public void clearBuffer(ByteBuffer data) {
        ClearMemoryRequest request = new ClearMemoryRequest();
        request.data = data;
        request.size = data.capacity();

        nativeImplementation.clearBuffer(request);
    }

}
