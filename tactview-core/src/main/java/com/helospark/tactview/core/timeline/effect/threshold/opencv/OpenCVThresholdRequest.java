package com.helospark.tactview.core.timeline.effect.threshold.opencv;

import java.nio.ByteBuffer;
import java.util.List;

import com.sun.jna.Structure;

public class OpenCVThresholdRequest extends Structure implements Structure.ByReference {
    public ByteBuffer output;
    public ByteBuffer input;
    public int width;
    public int height;

    public int blockSize;
    public int addedConstant;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("output", "input", "width", "height", "blockSize", "addedConstant");
    }
}
