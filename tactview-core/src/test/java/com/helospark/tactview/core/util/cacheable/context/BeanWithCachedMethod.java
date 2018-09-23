package com.helospark.tactview.core.util.cacheable.context;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class BeanWithCachedMethod {
    private Integer invocationCount = 0;

    @Cacheable
    public int cachedMethod() {
        return ++invocationCount;
    }
}
