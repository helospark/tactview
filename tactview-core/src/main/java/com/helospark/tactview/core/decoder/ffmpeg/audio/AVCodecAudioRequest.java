package com.helospark.tactview.core.decoder.ffmpeg.audio;

import java.util.List;

import com.helospark.tactview.core.decoder.ffmpeg.FFMpegFrame;
import com.sun.jna.Structure;

public class AVCodecAudioRequest extends Structure implements Structure.ByReference {
    public int numberOfFrames;
    public long startMicroseconds;

    public int numberOfChannels;
    public FFMpegFrame frames;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("numberOfFrames", "startMicroseconds", "numberOfChannels", "frames");
    }
}
