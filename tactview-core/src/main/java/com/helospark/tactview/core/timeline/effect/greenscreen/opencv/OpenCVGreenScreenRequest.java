package com.helospark.tactview.core.timeline.effect.greenscreen.opencv;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVGreenScreenRequest extends Structure implements Structure.ByReference {
    public ByteBuffer output;
    public ByteBuffer input;
    public int width;
    public int height;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("output", "input", "width", "height");
    }

}
