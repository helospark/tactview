package com.helospark.tactview.core.timeline.effect.stabilize.impl;

import static com.helospark.tactview.core.util.conditional.TactviewPlatform.LINUX;

import com.helospark.tactview.core.util.conditional.ConditionalOnPlatform;
import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@ConditionalOnPlatform(LINUX) // until we get compilation working on other platforms
@NativeImplementation("opencvvideostab")
public interface OpenCVStabilizeVideoService extends Library {

    int initializeStabilizer(StabilizationInitRequest request);

    void addFrame(AddStabilizeFrameRequest request);

    void finishedAddingFrames(int index);

    void createStabilizedFrame(StabilizeFrameRequest request);

    void deallocate(int index);
}
