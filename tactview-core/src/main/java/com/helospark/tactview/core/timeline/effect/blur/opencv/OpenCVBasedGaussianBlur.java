package com.helospark.tactview.core.timeline.effect.blur.opencv;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvblur")
public interface OpenCVBasedGaussianBlur extends Library {

    public void applyGaussianBlur(OpenCVGaussianBlurRequest request);

}
