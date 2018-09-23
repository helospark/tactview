package com.helospark.tactview.core.util.cacheable.context;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class CacheableBeanWithNoEmptyConstructor {
    private BeanWithNoCacheable dependency;

    public CacheableBeanWithNoEmptyConstructor(BeanWithNoCacheable dependency) {
        this.dependency = dependency;
    }

    @Cacheable
    public Integer cachedMethod() {
        return dependency.nonCachedMethod();
    }

}
