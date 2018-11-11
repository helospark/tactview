package com.helospark.tactview.core.decoder.ffmpeg.audio;

import java.util.List;

import com.sun.jna.Structure;

public class AVCodecAudioMetadataResponse extends Structure implements Structure.ByValue {
    public int sampleRate;
    public int channels;
    public int bytesPerSample;
    public long lengthInMicroseconds;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("sampleRate", "channels", "bytesPerSample", "lengthInMicroseconds");
    }
}
