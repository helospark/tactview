package com.helospark.tactview.core.render.ffmpeg;

import java.util.List;

import com.sun.jna.Structure;

public class CodecInformation extends Structure implements Structure.ByReference {
    public String id;
    public String longName;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("id", "longName");
    }
}
