package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.stringinterpolator.impl;

import java.util.function.Function;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.stringinterpolator.StringInterpolatorFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;

public class StandardStringInterpolatorFactory implements StringInterpolatorFactory {
    private String id;
    private Function<StringProvider, StringInterpolator> supplier;

    public StandardStringInterpolatorFactory(String id, Function<StringProvider, StringInterpolator> supplier) {
        this.id = id;
        this.supplier = supplier;
    }

    @Override
    public StringInterpolator createInterpolator(StringProvider previousInterpolator) {
        return supplier.apply(previousInterpolator);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean doesSuppert(String id) {
        return this.id.equals(id);
    }

}
