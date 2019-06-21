package com.helospark.tactview.core.render.ffmpeg;

import java.util.List;

import com.sun.jna.Structure;

public class NativePair extends Structure implements Structure.ByReference {
    public String key;
    public String value;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("key", "value");
    }
}
