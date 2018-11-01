package com.helospark.tactview.core.timeline.effect.cartoon.opencv;

import java.nio.ByteBuffer;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVCartoonRequest extends Structure implements Structure.ByReference {
    public ByteBuffer output;
    public ByteBuffer input;
    public int width;
    public int height;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("output", "input", "width", "height");
    }
}
