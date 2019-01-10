package com.helospark.tactview.core.timeline.effect.distort.impl;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvlensdistort")
public interface OpenCVBasedLensDistort extends Library {

    public void lensDistort(OpenCVLensDistortRequest request);

}
