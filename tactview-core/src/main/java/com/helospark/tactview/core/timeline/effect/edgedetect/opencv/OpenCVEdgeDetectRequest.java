package com.helospark.tactview.core.timeline.effect.edgedetect.opencv;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVEdgeDetectRequest extends Structure implements Structure.ByReference {
    public ByteBuffer output;
    public ByteBuffer input;
    public int width;
    public int height;
    public double lowThreshold;
    public double highThresholdMultiplier;
    public int apertureSize;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("output", "input", "width", "height", "lowThreshold", "highThresholdMultiplier", "apertureSize");
    }
}
