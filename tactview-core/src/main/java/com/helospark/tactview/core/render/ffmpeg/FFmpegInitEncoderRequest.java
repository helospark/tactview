package com.helospark.tactview.core.render.ffmpeg;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class FFmpegInitEncoderRequest extends Structure implements Structure.ByReference {
    public String fileName;
    public int actualWidth;
    public int actualHeight;
    public int renderWidth;
    public int renderHeight;
    public double fps;

    public int audioChannels;
    public int bytesPerSample;
    public int sampleRate;

    public int videoBitRate;
    public int audioBitRate;
    public int audioSampleRate;
    public String videoCodec;
    public String audioCodec;
    public String videoPixelFormat;
    public String videoPreset;

    public NativeMap metadata;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("fileName", "actualWidth", "actualHeight", "renderWidth", "renderHeight", "fps", "audioChannels", "bytesPerSample", "sampleRate",
                "videoBitRate", "audioBitRate", "audioSampleRate", "videoCodec", "audioCodec", "videoPixelFormat", "videoPreset", "metadata");
    }
}
