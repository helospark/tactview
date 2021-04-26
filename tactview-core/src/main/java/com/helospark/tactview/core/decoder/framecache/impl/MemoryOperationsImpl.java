package com.helospark.tactview.core.decoder.framecache.impl;

import java.nio.ByteBuffer;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

@NativeImplementation("memoryoperations")
public interface MemoryOperationsImpl extends Library {

    public Pointer allocateDirect(int size);

    public void free(ByteBuffer data);

}
