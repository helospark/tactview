package com.helospark.tactview.core.timeline.effect.histogramequization.opencv;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvhistogram")
public interface OpenCVHistogramEquizerImplementation extends Library {

    void equizeHistogram(OpenCVHistogramEquizationRequest request);

}
