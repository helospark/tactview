package com.helospark.tactview.core.util.jpaplugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NativeImplementation {

    /**
     * Name of the native library without extension.
     * @return name of the native library
     */
    public String value();

}
