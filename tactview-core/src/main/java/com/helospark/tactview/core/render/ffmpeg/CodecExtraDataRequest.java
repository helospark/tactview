package com.helospark.tactview.core.render.ffmpeg;

import java.util.List;

import com.sun.jna.Structure;

public class CodecExtraDataRequest extends Structure implements Structure.ByReference {
    public String fileName;
    public String videoCodec;

    public CodecInformation availablePixelFormats;
    public int availablePixelFormatNumber;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("fileName", "videoCodec", "availablePixelFormats", "availablePixelFormatNumber");
    }
}
