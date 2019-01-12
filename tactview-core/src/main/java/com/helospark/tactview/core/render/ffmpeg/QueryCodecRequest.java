package com.helospark.tactview.core.render.ffmpeg;

import java.util.List;

import com.sun.jna.Structure;

public class QueryCodecRequest extends Structure implements Structure.ByReference {
    public CodecInformation videoCodecs;
    public CodecInformation audioCodecs;

    public int videoCodecNumber;
    public int audioCodecNumber;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("videoCodecs", "audioCodecs", "videoCodecNumber", "audioCodecNumber");
    }
}
