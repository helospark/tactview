package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class EmptyByteBufferFactory {

    @Cacheable(cacheTimeInMilliseconds = 1_000_000)
    public ByteBuffer createEmptyByteImage(int width, int height) {
        int size = width * height * 4;
        return ByteBuffer.allocateDirect(size);
    }

}
