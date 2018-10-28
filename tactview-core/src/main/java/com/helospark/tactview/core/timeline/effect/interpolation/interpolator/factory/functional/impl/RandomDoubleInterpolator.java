package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.impl;

import java.math.BigDecimal;
import java.util.Random;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

public class RandomDoubleInterpolator implements DoubleInterpolator {
    private int min;
    private int max;
    private int seed;
    private BigDecimal changeScale;
    private LinearInterpolator linearInterpolator = new LinearInterpolator();

    public RandomDoubleInterpolator(int min, int max, BigDecimal changeScale) {
        this.min = min;
        this.max = max;
        this.changeScale = changeScale;
        this.seed = new Random().nextInt();
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        BigDecimal currentSeconds = position.getSeconds();
        BigDecimal previousSecond = currentSeconds.subtract(currentSeconds.remainder(changeScale));
        BigDecimal nextSeconds = currentSeconds.add(changeScale);

        double currentDouble = getValueAt(previousSecond);
        double nextDouble = getValueAt(nextSeconds);

        return linearInterpolator.interpolate(new double[]{previousSecond.doubleValue(), nextSeconds.doubleValue()}, new double[]{currentDouble, nextDouble})
                .value(currentSeconds.doubleValue());
    }

    private double getValueAt(BigDecimal seconds) {
        int seedAtCurrentPosition = seconds.multiply(changeScale.multiply(BigDecimal.valueOf(1000))).intValue() * seed;
        Random random = new Random(seedAtCurrentPosition);
        return random.nextDouble() * (max - min) + min;
    }

    @Override
    public EffectInterpolator cloneInterpolator() {
        RandomDoubleInterpolator result = new RandomDoubleInterpolator(min, max, changeScale);
        result.seed = seed;
        return result;
    }
}
