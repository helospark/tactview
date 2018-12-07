package com.helospark.tactview.core.timeline.effect.contractbrightness;

import javax.annotation.Generated;

public class BrignessContrastServiceRequest {
    private double contrast;
    private double brightness;

    @Generated("SparkTools")
    private BrignessContrastServiceRequest(Builder builder) {
        this.contrast = builder.contrast;
        this.brightness = builder.brightness;
    }

    public double getContrast() {
        return contrast;
    }

    public double getBrightness() {
        return brightness;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private double contrast;
        private double brightness;

        private Builder() {
        }

        public Builder withContrast(double contrast) {
            this.contrast = contrast;
            return this;
        }

        public Builder withBrightness(double brightness) {
            this.brightness = brightness;
            return this;
        }

        public BrignessContrastServiceRequest build() {
            return new BrignessContrastServiceRequest(this);
        }
    }

}
