package com.helospark.tactview.core.timeline.effect.colorize;

import javax.annotation.Generated;

public class ColorTemperatureChangeRequest {
    double temperatureChange;
    double tintChange;

    @Generated("SparkTools")
    private ColorTemperatureChangeRequest(Builder builder) {
        this.temperatureChange = builder.temperatureChange;
        this.tintChange = builder.tintChange;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private double temperatureChange;
        private double tintChange;

        private Builder() {
        }

        public Builder withTemperatureChange(double temperatureChange) {
            this.temperatureChange = temperatureChange;
            return this;
        }

        public Builder withTintChange(double tintChange) {
            this.tintChange = tintChange;
            return this;
        }

        public ColorTemperatureChangeRequest build() {
            return new ColorTemperatureChangeRequest(this);
        }
    }
}
