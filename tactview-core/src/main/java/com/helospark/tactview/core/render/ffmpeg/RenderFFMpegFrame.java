package com.helospark.tactview.core.render.ffmpeg;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class RenderFFMpegFrame extends Structure implements Structure.ByReference {
    public ByteBuffer imageData;
    public ByteBuffer audioData;
    public int numberOfAudioSamples;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("imageData", "audioData", "numberOfAudioSamples");
    }

}
