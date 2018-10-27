package com.helospark.tactview.core.timeline.effect.denoise.opencv;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVDenoiseRequest extends Structure implements Structure.ByReference {
    public ByteBuffer output;
    public ByteBuffer input;
    public int width;
    public int height;

    public Integer templateWindowSize;
    public Integer searchWindowSize;
    public Double strength;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("output", "input", "width", "height", "templateWindowSize", "searchWindowSize", "strength");
    }

}
