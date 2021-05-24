package com.helospark.tactview.core.util.memoryoperations;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("nativememoryoperations")
public interface NativeMemoryOperations extends Library {

    public void copyBuffer(CopyMemoryRequest request);

}
