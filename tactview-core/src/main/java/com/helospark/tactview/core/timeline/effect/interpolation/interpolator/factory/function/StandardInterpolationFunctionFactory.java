package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function;

import java.util.function.Supplier;

import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.DoubleInterpolatorFactory;

public class StandardInterpolationFunctionFactory implements DoubleInterpolatorFactory {
    private String id;
    private Supplier<UnivariateInterpolator> factory;

    public StandardInterpolationFunctionFactory(String id, Supplier<UnivariateInterpolator> factory) {
        this.id = id;
        this.factory = factory;
    }

    @Override
    public DoubleInterpolator createInterpolator(DoubleInterpolator previousInterpolator) {
        if (previousInterpolator instanceof MultiKeyframeBasedDoubleInterpolator) {
            UnivariateInterpolator newInterpolator = factory.get();

            MultiKeyframeBasedDoubleInterpolator multiKeyframeBasedDoubleInterpolator = (MultiKeyframeBasedDoubleInterpolator) previousInterpolator;
            MultiKeyframeBasedDoubleInterpolator clone = multiKeyframeBasedDoubleInterpolator.deepClone();
            clone.setInterpolatorFunction(newInterpolator);
            return clone;
        }
        throw new IllegalArgumentException("Changing interpolator function is only possible for MultiKeyframeBasedDoubleInterpolator");
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
