package com.helospark.tactview.core.timeline.effect.rotate;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVRotateRequest extends Structure implements Structure.ByReference {
    public ByteBuffer input;
    public int originalWidth;
    public int originalHeight;

    public ByteBuffer output;
    public int newWidth;
    public int newHeight;

    public int rotationPointX;
    public int rotationPointY;

    public double rotationDegrees;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("input", "originalWidth", "originalHeight", "output", "newWidth", "newHeight", "rotationPointX", "rotationPointY", "rotationDegrees");
    }

}
