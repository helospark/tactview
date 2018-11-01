package com.helospark.tactview.core.timeline.effect.pencil.opencv;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("opencvpencil")
public interface OpenCVPencilSketchImplementation extends Library {

    public void pencilSketch(OpenCVPencilSketchRequest request);

}
