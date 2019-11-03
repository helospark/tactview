package com.helospark.tactview.core.render.proxy.ffmpeg;

import java.nio.ByteBuffer;
import java.util.List;

import com.sun.jna.Structure;

public class FFmpegFrameWithFrameNumber extends Structure implements Structure.ByReference {
    public int frame;
    public ByteBuffer data;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("frame", "data");
    }

}
