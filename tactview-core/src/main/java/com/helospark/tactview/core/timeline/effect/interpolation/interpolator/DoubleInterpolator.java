package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.math.BigDecimal;

import com.helospark.tactview.core.timeline.TimelinePosition;

public interface DoubleInterpolator extends EffectInterpolator {
    static final BigDecimal interpolationResolition = new BigDecimal("0.001");
    static final BigDecimal two = BigDecimal.valueOf(2);

    @Override
    public Double valueAt(TimelinePosition position);

    @Override
    public abstract DoubleInterpolator deepClone();

    public default BigDecimal integrate(TimelinePosition from, TimelinePosition to) {
        TimelinePosition currentPosition = from;
        BigDecimal area = BigDecimal.ZERO;

        while (currentPosition.isLessThan(to)) {
            BigDecimal lowValue = BigDecimal.valueOf(valueAt(currentPosition));
            BigDecimal highValue = BigDecimal.valueOf(valueAt(currentPosition.add(interpolationResolition)));

            // trapezoid area
            area = area.add(lowValue.add(highValue).divide(two).multiply(interpolationResolition));

            currentPosition.add(interpolationResolition);
        }
        return area;
    }

}
