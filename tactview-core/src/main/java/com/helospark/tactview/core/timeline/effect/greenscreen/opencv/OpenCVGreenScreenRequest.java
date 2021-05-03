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

    public int hueMin, hueMax;
    public int saturationMin, saturationMax;
    public int valueMin, valueMax;

    public int spillRemovalEnabled;
    public int spillDeltaHue;
    public int spillSaturationThreshold;
    public int spillValueThreshold;

    public int enableEdgeBlur;
    public int edgeBlurRadius;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("output", "input", "width", "height",
                "hueMin", "hueMax",
                "saturationMin", "saturationMax",
                "valueMin", "valueMax",
                "spillRemovalEnabled",
                "spillDeltaHue", "spillSaturationThreshold", "spillValueThreshold",
                "enableEdgeBlur", "edgeBlurRadius");
    }

}
