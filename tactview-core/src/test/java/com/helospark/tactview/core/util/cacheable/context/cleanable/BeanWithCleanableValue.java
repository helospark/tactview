package com.helospark.tactview.core.util.cacheable.context.cleanable;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class BeanWithCleanableValue {

    @Cacheable(cacheTimeInMilliseconds = 1, size = 1)
    public CleanableValue cleanableCache(String value, MyCleaner cleaner) {
        return new CleanableValue(value, cleaner);
    }

}
