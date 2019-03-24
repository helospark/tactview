package com.helospark.tactview.core.timeline.effect.blur.service;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class RadialBlurRequest {
    int centerX, centerY;
    double angle;
    ReadOnlyClipImage inputImage;

    @Generated("SparkTools")
    private RadialBlurRequest(Builder builder) {
        this.centerX = builder.centerX;
        this.centerY = builder.centerY;
        this.angle = builder.angle;
        this.inputImage = builder.inputImage;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private int centerX;
        private int centerY;
        private double angle;
        private ReadOnlyClipImage inputImage;

        private Builder() {
        }

        public Builder withCenterX(int centerX) {
            this.centerX = centerX;
            return this;
        }

        public Builder withCenterY(int centerY) {
            this.centerY = centerY;
            return this;
        }

        public Builder withAngle(double angle) {
            this.angle = angle;
            return this;
        }

        public Builder withInputImage(ReadOnlyClipImage inputImage) {
            this.inputImage = inputImage;
            return this;
        }

        public RadialBlurRequest build() {
            return new RadialBlurRequest(this);
        }
    }
}
