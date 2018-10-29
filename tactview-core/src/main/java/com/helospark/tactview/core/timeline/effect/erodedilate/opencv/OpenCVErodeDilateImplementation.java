package com.helospark.tactview.core.timeline.effect.erodedilate.opencv;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencverodedilate")
public interface OpenCVErodeDilateImplementation extends Library {

    public void erodeDilate(OpenCVErodeDilateRequest request);

}
