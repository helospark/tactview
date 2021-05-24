package com.helospark.tactview.core.util.memoryoperations;

import java.nio.ByteBuffer;
import java.util.List;

import com.sun.jna.Structure;

public class CopyMemoryRequest extends Structure implements Structure.ByReference {
    public ByteBuffer from;
    public ByteBuffer to;
    public int size;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("from", "to", "size");
    }

}
