package com.helospark.tactview.core.render.ffmpeg;

import java.util.List;

import com.sun.jna.Structure;

public class NativeMap extends Structure implements Structure.ByReference {
    public int size;
    public NativePair data;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("size", "data");
    }
}
