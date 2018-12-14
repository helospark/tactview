package com.helospark.tactview.core.timeline.effect.sharpen.implementation;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvsharpen")
public interface OpenCVSharpenImplementation extends Library {

    public void sharpen(OpenCVSharpenRequest request);

}
