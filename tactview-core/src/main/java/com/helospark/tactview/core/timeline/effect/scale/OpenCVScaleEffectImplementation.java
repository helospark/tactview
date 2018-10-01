package com.helospark.tactview.core.timeline.effect.scale;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvscale")
public interface OpenCVScaleEffectImplementation extends Library {

    public void scaleImage(OpenCVScaleRequest scaleRequest);

}
