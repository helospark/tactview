package com.helospark.tactview.core.timeline.effect.greenscreen.opencv;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvgreenscreen")
public interface OpenCVGreenScreenImplementation extends Library {

    public void greenScreen(OpenCVGreenScreenRequest nativeRequest);

}
