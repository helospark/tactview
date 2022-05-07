package com.helospark.tactview.core.decoder.ffmpeg;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class FFmpegImageRequest extends Structure implements Structure.ByReference {
    public int width;
    public int height;
    public int numberOfFrames;
    public int useApproximatePosition;
    public long startMicroseconds;
    public String path;
    public FFMpegFrame frames;

    // output
    public int actualNumberOfFramesRead;
    public long endTimeInMs;

    public int useHardwareDecoding;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("width", "height", "numberOfFrames", "useApproximatePosition", "startMicroseconds", "path", "frames", "actualNumberOfFramesRead", "endTimeInMs", "useHardwareDecoding");
    }
}
