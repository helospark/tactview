package com.helospark.tactview.core.decoder.ffmpeg;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class FFMpegFrame extends Structure implements Structure.ByReference {
    public ByteBuffer data;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("data");
    }

}
