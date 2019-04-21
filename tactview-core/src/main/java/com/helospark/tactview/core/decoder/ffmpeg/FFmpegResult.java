package com.helospark.tactview.core.decoder.ffmpeg;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class FFmpegResult extends Structure implements Structure.ByValue {
    public double fps;
    public int width;
    public int height;
    public int bitRate;
    public long lengthInMicroseconds;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("fps", "width", "height", "bitRate", "lengthInMicroseconds");
    }

}
