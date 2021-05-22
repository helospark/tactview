package com.helospark.tactview.core.timeline.framemerge.nativelibrary;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class AlphaBlendRequest extends Structure implements Structure.ByReference {
    public ByteBuffer foreground;
    public ByteBuffer backgroundAndResult;
    public int width;
    public int height;
    public double alpha;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("foreground", "backgroundAndResult", "width", "height", "alpha");
    }
}