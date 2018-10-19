package com.helospark.tactview.core.render.ffmpeg;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class FFmpegClearEncoderRequest extends Structure implements Structure.ByReference {
    public int encoderIndex;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("encoderIndex");
    }
}
