package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator;

import java.util.function.Function;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;

public class StandardDoubleInterpolatorFactory implements DoubleInterpolatorFactory {
    private String id;
    private Function<DoubleProvider, DoubleInterpolator> supplier;

    public StandardDoubleInterpolatorFactory(String id, Function<DoubleProvider, DoubleInterpolator> supplier) {
        this.id = id;
        this.supplier = supplier;
    }

    @Override
    public DoubleInterpolator createInterpolator(DoubleProvider previousInterpolator) {
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
