package com.helospark.tactview.core.util.cacheable.context;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class BeanWithSomeCachedAndSomeNonCachedMethods {
    private Integer cachedMethodInvocationCount = 0;
    private Integer nonCachedMethodInvocationCount = 0;

    @Cacheable
    public int cachedMethod() {
        return ++cachedMethodInvocationCount;
    }

    public int nonCachedMethodInvocationCount() {
        return ++nonCachedMethodInvocationCount;
    }

}
