package com.helospark.tactview.core.timeline.effect.transform.implementation;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("genericconvolutionmatrix")
public interface OpenCVGenericTransformation extends Library {

    public void transform(OpenCVTransformRequest transformRequest);
}
