package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;

public class StepInterpolator implements UnivariateInterpolator {

    @Override
    public UnivariateFunction interpolate(double[] xval, double[] yval) throws MathIllegalArgumentException, DimensionMismatchException {
        return x -> {
            for (int i = 0; i < xval.length; ++i) {
                if (xval[i] >= x) {
                    return yval[i];
                }
            }
            throw new IllegalArgumentException("No values");
        };
    }

}
