package com.helospark.tactview.core.timeline.effect.denoise.opencv;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvdenoise")
public interface OpenCVBasedDenoiseEffect extends Library {

    public void denoise(OpenCVDenoiseRequest request);

}
