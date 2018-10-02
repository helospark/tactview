package com.helospark.tactview.core.timeline.effect.rotate;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvrotate")
public interface OpenCVRotateEffectImplementation extends Library {

    public void rotateImage(OpenCVRotateRequest request);

}
