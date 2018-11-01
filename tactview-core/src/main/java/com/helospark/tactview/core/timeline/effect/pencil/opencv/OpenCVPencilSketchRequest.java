package com.helospark.tactview.core.timeline.effect.pencil.opencv;

import java.nio.ByteBuffer;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVPencilSketchRequest extends Structure implements Structure.ByReference {
    public ByteBuffer output;
    public ByteBuffer input;
    public int width;
    public int height;

    public double sigmaS;
    public double sigmaR;
    public double shadeFactor;

    public boolean color;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("output", "input", "width", "height", "sigmaS", "sigmaR", "shadeFactor", "color");
    }

}
