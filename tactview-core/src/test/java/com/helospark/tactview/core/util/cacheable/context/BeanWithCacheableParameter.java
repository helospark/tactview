package com.helospark.tactview.core.util.cacheable.context;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class BeanWithCacheableParameter {
    private Integer invocationCount = 0;

    @Cacheable
    public Integer getInvocationCount(Object parameter) {
        return ++invocationCount;
    }
}
