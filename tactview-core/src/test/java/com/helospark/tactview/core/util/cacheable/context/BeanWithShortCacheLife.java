package com.helospark.tactview.core.util.cacheable.context;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class BeanWithShortCacheLife {
    private Integer invocationCount = 0;

    @Cacheable(cacheTimeInMilliseconds = 100)
    public Integer getInvocationCount() {
        return ++invocationCount;
    }
}
