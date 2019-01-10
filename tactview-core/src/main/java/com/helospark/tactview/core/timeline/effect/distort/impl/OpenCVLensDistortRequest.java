package com.helospark.tactview.core.timeline.effect.distort.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVLensDistortRequest extends Structure implements Structure.ByReference {
    public ByteBuffer output;
    public ByteBuffer input;
    public int width;
    public int height;

    public int opticalCenterX;
    public int opticalCenterY;
    public double focalLength;

    public double k1;
    public double k2;
    public double k3;
    public double p1;
    public double p2;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("output", "input", "width", "height", "opticalCenterX", "opticalCenterY", "focalLength", "k1", "k2", "k3", "p1", "p2");
    }

}
