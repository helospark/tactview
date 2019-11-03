package com.helospark.tactview.core.render.proxy.ffmpeg;

import java.util.List;

import com.sun.jna.Structure;

public class InitializeReadJobRequest extends Structure implements Structure.ByReference {
    public String path;
    public int width;
    public int height;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("path", "width", "height");
    }

}
