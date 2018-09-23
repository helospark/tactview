package com.helospark.tactview.core.util.cacheable.context;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class CacheableWithVoid {

    @Cacheable
    public void getInvocationCount() {
    }
}
