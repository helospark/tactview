package com.helospark.tactview.core.util.cacheable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.junit.jupiter.api.Test;

public class InterpolatorTest {

    @Test
    public void testApacheInterpolator() {
        LinearInterpolator interpolator = new LinearInterpolator();
        PolynomialSplineFunction interpolateFunction = interpolator.interpolate(new double[] { 2.0, 3.0, 4.0, 5.0 }, new double[] { 2.0, 3.0, 4.0, 5.0 });

        assertThat(interpolateFunction.value(2.0), closeTo(2.0, 0.001));
        assertThat(interpolateFunction.value(2.5), closeTo(2.5, 0.001));
        assertThrows(OutOfRangeException.class, () -> interpolateFunction.value(1));
    }

}
