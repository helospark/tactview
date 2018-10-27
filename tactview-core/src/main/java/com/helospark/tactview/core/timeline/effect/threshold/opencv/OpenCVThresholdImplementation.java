package com.helospark.tactview.core.timeline.effect.threshold.opencv;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvthreshold")
public interface OpenCVThresholdImplementation extends Library {

    public void threshold(OpenCVThresholdRequest request);

}
