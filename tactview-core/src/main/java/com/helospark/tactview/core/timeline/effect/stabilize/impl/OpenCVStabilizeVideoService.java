package com.helospark.tactview.core.timeline.effect.stabilize.impl;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvvideostab")
public interface OpenCVStabilizeVideoService extends Library {

    int initializeStabilizer(StabilizationInitRequest request);

    void addFrame(AddStabilizeFrameRequest request);

    void finishedAddingFrames();

    void createStabilizedFrame(StabilizeFrameRequest request);
}
