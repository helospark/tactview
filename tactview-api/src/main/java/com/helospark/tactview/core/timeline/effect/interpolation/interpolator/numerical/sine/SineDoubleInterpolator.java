package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.sine;

import javax.annotation.Generated;

import com.helospark.tactview.core.DesSerFactory;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

public class SineDoubleInterpolator implements DoubleInterpolator {
    double frequency;
    double startOffset;
    double minValue;
    double maxValue;

    @Generated("SparkTools")
    private SineDoubleInterpolator(Builder builder) {
        this.frequency = builder.frequency;
        this.startOffset = builder.startOffset;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return SineDoubleInterpolatorFactory.class;
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        double interval = (maxValue - minValue);
        return ((Math.sin(startOffset + position.getSeconds().doubleValue() * frequency) + 1.0) / 2.0) * interval + minValue;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getStartOffset() {
        return startOffset;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public void setStartOffset(double startOffset) {
        this.startOffset = startOffset;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public DoubleInterpolator deepClone() {
        return SineDoubleInterpolator.builder()
                .withFrequency(frequency)
                .withMaxValue(maxValue)
                .withMinValue(minValue)
                .withStartOffset(startOffset)
                .build();
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private double frequency;
        private double startOffset;
        private double minValue;
        private double maxValue;

        private Builder() {
        }

        public Builder withFrequency(double frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder withStartOffset(double startOffset) {
            this.startOffset = startOffset;
            return this;
        }

        public Builder withMinValue(double minValue) {
            this.minValue = minValue;
            return this;
        }

        public Builder withMaxValue(double maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public SineDoubleInterpolator build() {
            return new SineDoubleInterpolator(this);
        }
    }

}
