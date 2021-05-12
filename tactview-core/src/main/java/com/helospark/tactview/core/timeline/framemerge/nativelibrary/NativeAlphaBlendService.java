package com.helospark.tactview.core.timeline.framemerge.nativelibrary;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("nativealphablend")
public interface NativeAlphaBlendService extends Library {

    void normalAlphablend(AlphaBlendRequest request);

}
