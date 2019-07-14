package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.math.BigDecimal;

import com.helospark.tactview.core.timeline.TimelineLength;
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

            currentPosition = currentPosition.add(interpolationResolition);
        }
        return area;
    }

    public default BigDecimal integrateUntil(TimelinePosition start, TimelineLength untilValue, BigDecimal max) {
        BigDecimal area = BigDecimal.ZERO;
        while (area.compareTo(untilValue.getSeconds()) < 0 && start.getSeconds().compareTo(max) < 0) {
            BigDecimal integralValue = this.integrate(TimelinePosition.ofZero(), start.add(BigDecimal.ONE));
            BigDecimal newArea = integralValue;
            if (newArea.compareTo(untilValue.getSeconds()) < 0) {
                area = newArea;
                start = start.add(BigDecimal.ONE);
            } else {
                break;
            }
        }

        while (area.compareTo(untilValue.getSeconds()) < 0 && start.getSeconds().compareTo(max) < 0) {
            BigDecimal lowValue = BigDecimal.valueOf(valueAt(start));
            BigDecimal highValue = BigDecimal.valueOf(valueAt(start.add(interpolationResolition)));

            // trapezoid area
            area = area.add(lowValue.add(highValue).divide(two).multiply(interpolationResolition));

            start = start.add(interpolationResolition);
        }

        return start.getSeconds();
    }

}
