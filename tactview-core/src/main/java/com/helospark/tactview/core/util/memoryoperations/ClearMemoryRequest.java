package com.helospark.tactview.core.util.memoryoperations;

import java.nio.ByteBuffer;
import java.util.List;

import com.sun.jna.Structure;

public class ClearMemoryRequest extends Structure implements Structure.ByReference {
    public ByteBuffer data;
    public int size;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("data", "size");
    }

}
