package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator;

import java.util.function.Function;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;

public class StandardDoubleInterpolatorFactory implements DoubleInterpolatorFactory {
    private String id;
    private Function<DoubleInterpolator, DoubleInterpolator> supplier;

    public StandardDoubleInterpolatorFactory(String id, Function<DoubleInterpolator, DoubleInterpolator> supplier) {
        this.id = id;
        this.supplier = supplier;
    }

    @Override
    public DoubleInterpolator createInterpolator(DoubleInterpolator previousInterpolator) {
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
