package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;

public class StepInterpolator implements UnivariateInterpolator {

    @Override
    public UnivariateFunction interpolate(double[] xval, double[] yval) throws MathIllegalArgumentException, DimensionMismatchException {
        return x -> {
            if (xval.length == 0) {
                throw new IllegalArgumentException("No values");
            }
            int i = 0;
            while (i < xval.length && xval[i] <= x) {
                ++i;
            }
            if (i == 0) {
                return yval[0];
            } else {
                return yval[i - 1];
            }
        };
    }

}
