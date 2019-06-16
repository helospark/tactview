package com.helospark.tactview.core.timeline.effect.stabilize.impl;

import java.nio.ByteBuffer;
import java.util.List;

import com.sun.jna.Structure;

public class StabilizeFrameRequest extends Structure implements Structure.ByReference {
    public int index;
    public ByteBuffer input;
    public ByteBuffer output;
    public int width;
    public int height;
    public int frameIndex;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("index", "input", "output", "width", "height", "frameIndex");
    }
}
