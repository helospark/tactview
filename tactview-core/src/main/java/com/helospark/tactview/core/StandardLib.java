package com.helospark.tactview.core;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("c")
public interface StandardLib extends Library {

    void printf(String format, Object... args);
}
