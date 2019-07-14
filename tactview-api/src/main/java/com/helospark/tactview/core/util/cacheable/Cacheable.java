package com.helospark.tactview.core.util.cacheable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {
    int size() default 20;

    int cacheTimeInMilliseconds() default 60000;
}
