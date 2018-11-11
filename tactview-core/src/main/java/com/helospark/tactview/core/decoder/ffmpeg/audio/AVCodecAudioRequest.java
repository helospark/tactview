package com.helospark.tactview.core.decoder.ffmpeg.audio;

import java.util.List;

import com.helospark.tactview.core.decoder.ffmpeg.FFMpegFrame;
import com.sun.jna.Structure;

public class AVCodecAudioRequest extends Structure implements Structure.ByReference {
    public long lengthInMicroseconds;
    public long startMicroseconds;

    public int numberOfChannels;
    public FFMpegFrame channels;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("lengthInMicroseconds", "startMicroseconds", "numberOfChannels", "frames");
    }
}
