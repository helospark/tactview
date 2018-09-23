package com.helospark.tactview.core.util.cacheable.context;

import com.helospark.lightdi.annotation.Component;

@Component
public class BeanWithNoCacheable {
    private Integer invocationCount = 0;

    public Integer nonCachedMethod() {
        return ++invocationCount;
    }

}
