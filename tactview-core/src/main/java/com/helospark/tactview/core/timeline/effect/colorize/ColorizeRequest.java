package com.helospark.tactview.core.timeline.effect.colorize;

import javax.annotation.Generated;

public class ColorizeRequest {
    private double hueChange;
    private double saturationChange;
    private double valueChange;

    @Generated("SparkTools")
    private ColorizeRequest(Builder builder) {
        this.hueChange = builder.hueChange;
        this.saturationChange = builder.saturationChange;
        this.valueChange = builder.valueChange;
    }

    public double getHueChange() {
        return hueChange;
    }

    public double getSaturationChange() {
        return saturationChange;
    }

    public double getValueChange() {
        return valueChange;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private double hueChange;
        private double saturationChange;
        private double valueChange;

        private Builder() {
        }

        public Builder withHueChange(double hueChange) {
            this.hueChange = hueChange;
            return this;
        }

        public Builder withSaturationChange(double saturationChange) {
            this.saturationChange = saturationChange;
            return this;
        }

        public Builder withValueChange(double valueChange) {
            this.valueChange = valueChange;
            return this;
        }

        public ColorizeRequest build() {
            return new ColorizeRequest(this);
        }
    }
}
