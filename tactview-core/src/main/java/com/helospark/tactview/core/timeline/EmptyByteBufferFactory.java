package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;

@Component
public class EmptyByteBufferFactory {

    public ByteBuffer createEmptyByteImage(int width, int height) {
        int size = width * height * 4;
        return GlobalMemoryManagerAccessor.memoryManager.requestBuffer(size);
    }

}
