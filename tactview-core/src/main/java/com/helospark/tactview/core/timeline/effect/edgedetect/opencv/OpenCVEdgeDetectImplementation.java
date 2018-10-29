package com.helospark.tactview.core.timeline.effect.edgedetect.opencv;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvedgedetect")
public interface OpenCVEdgeDetectImplementation extends Library {

    public void edgeDetect(OpenCVEdgeDetectRequest request);

}
