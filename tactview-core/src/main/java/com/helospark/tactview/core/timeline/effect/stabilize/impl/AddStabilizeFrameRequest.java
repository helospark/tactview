package com.helospark.tactview.core.timeline.effect.stabilize.impl;

import java.nio.ByteBuffer;
import java.util.List;

import com.sun.jna.Structure;

public class AddStabilizeFrameRequest extends Structure implements Structure.ByReference {
    public ByteBuffer input;
    public int width;
    public int height;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("input", "width", "height");
    }
}
