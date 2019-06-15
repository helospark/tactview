package com.helospark.tactview.core.timeline.effect.stabilize.impl;

import java.util.List;

import com.sun.jna.Structure;

public class StabilizationInitRequest extends Structure implements Structure.ByReference {
    public int radius;
    public int width;
    public int height;
    public String motionFile;
    public String motion2File;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("radius", "width", "height", "motionFile", "motion2File");
    }
}
