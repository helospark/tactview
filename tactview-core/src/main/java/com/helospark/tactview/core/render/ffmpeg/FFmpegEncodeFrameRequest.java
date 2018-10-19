package com.helospark.tactview.core.render.ffmpeg;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.decoder.ffmpeg.FFMpegFrame;
import com.sun.jna.Structure;

public class FFmpegEncodeFrameRequest extends Structure implements Structure.ByReference {
    public int encoderIndex;
    public int startFrameIndex;
    public FFMpegFrame frames;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("encoderIndex", "startFrameIndex", "frames");
    }
}
