package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl;

import java.math.BigDecimal;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;

import com.helospark.tactview.core.DesSerFactory;
import com.helospark.tactview.core.RepeatableRandom;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

public class RandomDoubleInterpolator implements DoubleInterpolator {
    double min;
    double max;
    RepeatableRandom repeatableRandom;
    BigDecimal changeScale;
    LinearInterpolator linearInterpolator = new LinearInterpolator();

    public RandomDoubleInterpolator(double min, double max, BigDecimal changeScale) {
        this.min = min;
        this.max = max;
        this.changeScale = changeScale;
        this.repeatableRandom = new RepeatableRandom();
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        BigDecimal currentSeconds = position.getSeconds();
        BigDecimal previousSecond = currentSeconds.subtract(currentSeconds.remainder(changeScale));
        BigDecimal nextSeconds = previousSecond.add(changeScale);

        double currentDouble = getValueAt(previousSecond);
        double nextDouble = getValueAt(nextSeconds);

        return linearInterpolator.interpolate(new double[]{previousSecond.doubleValue(), nextSeconds.doubleValue()}, new double[]{currentDouble, nextDouble})
                .value(currentSeconds.doubleValue());
    }

    private double getValueAt(BigDecimal seconds) {
        return repeatableRandom.nextDouble(seconds) * (max - min) + min;
    }

    @Override
    public RandomDoubleInterpolator deepClone() {
        RandomDoubleInterpolator result = new RandomDoubleInterpolator(min, max, changeScale);
        result.repeatableRandom = repeatableRandom;
        return result;
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return RandomDoubleInterpolatorFactory.class;
    }

    public void changeScale(BigDecimal changeScale) {
        this.changeScale = changeScale;
    }

    public BigDecimal getChangeScale() {
        return changeScale;
    }

}
