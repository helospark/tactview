package com.helospark.tactview.core.timeline.effect.transform.implementation;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class OpenCVTransformRequest extends Structure implements Structure.ByReference {
    public ByteBuffer output;
    public ByteBuffer input;
    public int width;
    public int height;

    public Pointer matrix;
    public int matrixWidth;
    public int matrixHeight;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("output", "input", "width", "height", "matrixWidth", "matrixHeight", "matrix");
    }
}
