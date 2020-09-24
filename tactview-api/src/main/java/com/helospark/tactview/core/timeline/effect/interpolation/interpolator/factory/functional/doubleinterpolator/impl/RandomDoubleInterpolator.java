package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl;

import java.math.BigDecimal;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.RepeatableRandom;

public class RandomDoubleInterpolator implements DoubleInterpolator {
    double min;
    double max;
    RepeatableRandom repeatableRandom;
    BigDecimal changeScale;
    LinearInterpolator linearInterpolator = new LinearInterpolator();

    double initialMin;
    double initialMax;
    RepeatableRandom initialRepeatableRandom;
    BigDecimal initialChangeScale;
    LinearInterpolator initialLinearInterpolator;

    public RandomDoubleInterpolator(double min, double max, BigDecimal changeScale) {
        this.min = min;
        this.max = max;
        this.changeScale = changeScale;
        this.repeatableRandom = new RepeatableRandom();

        initialMin = min;
        initialMax = max;
        initialRepeatableRandom = repeatableRandom;
        initialChangeScale = changeScale;
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

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public int getSeed() {
        return this.repeatableRandom.getSeed();
    }

    public void setSeed(int seed) {
        this.repeatableRandom = new RepeatableRandom(seed);
    }

    public void setChangeScale(BigDecimal changeScale) {
        this.changeScale = changeScale;
    }

    public BigDecimal getChangeScale() {
        return changeScale;
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

    @Override
    public void resetToDefaultValue() {
        this.min = initialMin;
        this.max = initialMax;
        this.repeatableRandom = initialRepeatableRandom;
        this.changeScale = initialChangeScale;
    }

}
