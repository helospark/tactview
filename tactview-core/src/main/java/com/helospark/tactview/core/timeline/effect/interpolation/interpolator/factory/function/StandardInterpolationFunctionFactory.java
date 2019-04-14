package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function;

import java.util.function.Supplier;

import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

public class StandardInterpolationFunctionFactory implements MultiKeyframeDoubleInterpolatorFunctionFactory {
    private String id;
    private Supplier<UnivariateInterpolator> factory;

    public StandardInterpolationFunctionFactory(String id, Supplier<UnivariateInterpolator> factory) {
        this.id = id;
        this.factory = factory;
    }

    @Override
    public UnivariateInterpolator createInterpolator() {
        return factory.get();
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
