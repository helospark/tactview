package com.helospark.tactview.core.timeline.effect.scale;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVScaleRequest extends Structure implements Structure.ByReference {
    public ByteBuffer input;
    public ByteBuffer output;

    public int originalWidth;
    public int originalHeight;

    public int newWidth;
    public int newHeight;

    public int interpolationType;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("input", "output", "originalWidth", "originalHeight", "newWidth", "newHeight", "interpolationType");
    }

}
