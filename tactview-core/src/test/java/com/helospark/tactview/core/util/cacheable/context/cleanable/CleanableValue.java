package com.helospark.tactview.core.util.cacheable.context.cleanable;

import com.helospark.tactview.core.util.cacheable.CacheCleanable;

public class CleanableValue implements CacheCleanable {
    private String field;
    private MyCleaner cleaner;

    public CleanableValue(String field, MyCleaner cleaner) {
        this.field = field;
        this.cleaner = cleaner;
    }

    @Override
    public void clean() {
        cleaner.clean(field);
    }

}
