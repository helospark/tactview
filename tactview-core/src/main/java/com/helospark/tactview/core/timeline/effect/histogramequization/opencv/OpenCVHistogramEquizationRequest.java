package com.helospark.tactview.core.timeline.effect.histogramequization.opencv;

import java.nio.ByteBuffer;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVHistogramEquizationRequest extends Structure implements Structure.ByReference {
    public ByteBuffer output;
    public ByteBuffer input;
    public int width;
    public int height;

    public int adaptiveClipLimit;
    public int adaptiveKernelWidth;
    public int adaptiveKernelHeight;

    public int grayscale; // TODO: these should be boolean, but was not working for some reason
    public int adaptive;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("output", "input", "width", "height", "adaptiveClipLimit", "adaptiveKernelWidth", "adaptiveKernelHeight", "grayscale", "adaptive");
    }

}
