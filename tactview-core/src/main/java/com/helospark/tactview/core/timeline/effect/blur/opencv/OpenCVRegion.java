package com.helospark.tactview.core.timeline.effect.blur.opencv;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVRegion extends Structure implements Structure.ByReference {
    public int x;
    public int y;
    public int width;
    public int height;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("x", "y", "width", "height");
    }
}
