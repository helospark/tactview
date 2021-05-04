package com.helospark.tactview.core.decoder.ffmpeg;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class FFmpegImageRequest extends Structure implements Structure.ByReference {
    public int width;
    public int height;
    public int numberOfFrames;
    public int useApproximatePosition;
    public int disableSeeking;
    public long startMicroseconds;
    public String path;
    public FFMpegFrame frames;

    public long endTimeInMilliseconds;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("width", "height", "numberOfFrames", "useApproximatePosition", "disableSeeking", "startMicroseconds", "path", "frames", "endTimeInMilliseconds");
    }
}
