package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function;

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl.StepInterpolator;

@Configuration
public class StandardInterpolatorFunctionConfiguration {

    @Bean
    public StandardInterpolationFunctionFactory stepInterpolatorFactory() {
        return new StandardInterpolationFunctionFactory("stepInterpolator", () -> {
            return new StepInterpolator();
        });
    }

    @Bean
    public StandardInterpolationFunctionFactory linearInterpolatorFactory() {
        return new StandardInterpolationFunctionFactory("linearInterpolator", () -> {
            return new LinearInterpolator();
        });
    }

    @Bean
    public StandardInterpolationFunctionFactory splineInterpolatorFactory() {
        return new StandardInterpolationFunctionFactory("splineInterpolator", () -> {
            SplineInterpolator splineInterpolator = new SplineInterpolator();
            return createInterpolatorWithLinearFallback(splineInterpolator, 3);
        });
    }

    @Bean
    public StandardInterpolationFunctionFactory akimaInterpolatorFactory() {
        return new StandardInterpolationFunctionFactory("akimaInterpolator", () -> {
            AkimaSplineInterpolator splineInterpolator = new AkimaSplineInterpolator();
            return createInterpolatorWithLinearFallback(splineInterpolator, 5);
        });
    }

    @Bean
    public StandardInterpolationFunctionFactory newtonInterpolatorFactory() {
        return new StandardInterpolationFunctionFactory("newtonInterpolator", () -> {
            return new DividedDifferenceInterpolator();
        });
    }

    private UnivariateInterpolator createInterpolatorWithLinearFallback(UnivariateInterpolator splineInterpolator, int minimumValuesRequired) {
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        return (xvals, yvals) -> {
            if (xvals.length < minimumValuesRequired) {
                return linearInterpolator.interpolate(xvals, yvals);
            } else {
                return splineInterpolator.interpolate(xvals, yvals);
            }
        };
    }

}
