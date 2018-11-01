package com.helospark.tactview.core.timeline.effect.cartoon.opencv;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvcartoon")
public interface OpenCVCartoonEffectImplementation extends Library {

    public void cartoon(OpenCVCartoonRequest request);

}
