package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function;

import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

public interface MultiKeyframeDoubleInterpolatorFunctionFactory {
    public UnivariateInterpolator createInterpolator();

    public String getId();

    public boolean doesSuppert(String id);

}
