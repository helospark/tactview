package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.square;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class SquareDoubleInterpolator implements DoubleInterpolator {
    double onTime;
    double offTime;

    // TODO: offset

    double minValue;
    double maxValue;

    @Generated("SparkTools")
    private SquareDoubleInterpolator(Builder builder) {
        this.onTime = builder.onTime;
        this.offTime = builder.offTime;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return SquareDoubleInterpolatorFactory.class;
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        double period = onTime + offTime;
        double valueInSeconds = position.getSeconds().doubleValue();

        double currentPositionInWave = valueInSeconds % period;
        if (currentPositionInWave > onTime) {
            return minValue;
        } else {
            return maxValue;
        }
    }

    public void setOnTime(double onTime) {
        this.onTime = onTime;
    }

    public void setOffTime(double offTime) {
        this.offTime = offTime;
    }

    public double getOnTime() {
        return onTime;
    }

    public double getOffTime() {
        return offTime;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public DoubleInterpolator deepClone() {
        return SquareDoubleInterpolator.builder()
                .withMaxValue(maxValue)
                .withMinValue(minValue)
                .withOnTime(onTime)
                .withOffTime(offTime)
                .build();
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private double onTime;
        private double offTime;
        private double minValue;
        private double maxValue;

        private Builder() {
        }

        public Builder withOnTime(double onTime) {
            this.onTime = onTime;
            return this;
        }

        public Builder withOffTime(double offTime) {
            this.offTime = offTime;
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

        public SquareDoubleInterpolator build() {
            return new SquareDoubleInterpolator(this);
        }
    }

}
