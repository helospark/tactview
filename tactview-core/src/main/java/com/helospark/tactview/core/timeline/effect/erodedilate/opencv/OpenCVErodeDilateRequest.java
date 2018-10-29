package com.helospark.tactview.core.timeline.effect.erodedilate.opencv;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVErodeDilateRequest extends Structure implements Structure.ByReference {
    public ByteBuffer output;
    public ByteBuffer input;
    public int width;
    public int height;
    public int kernelWidth;
    public int kernelHeight;
    public boolean erode;
    public int shape;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("output", "input", "width", "height", "kernelWidth", "kernelHeight", "erode", "shape");
    }
}
