package com.helospark.tactview.core.timeline.effect.rotate;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class RotateServiceRequest {
    double angle;
    ReadOnlyClipImage image;

    double centerX;
    double centerY;

    @Generated("SparkTools")
    private RotateServiceRequest(Builder builder) {
        this.angle = builder.angle;
        this.image = builder.image;
        this.centerX = builder.centerX;
        this.centerY = builder.centerY;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private double angle;
        private ReadOnlyClipImage image;
        private double centerX;
        private double centerY;

        private Builder() {
        }

        public Builder withAngle(double angle) {
            this.angle = angle;
            return this;
        }

        public Builder withImage(ReadOnlyClipImage image) {
            this.image = image;
            return this;
        }

        public Builder withCenterX(double centerX) {
            this.centerX = centerX;
            return this;
        }

        public Builder withCenterY(double centery) {
            this.centerY = centery;
            return this;
        }

        public RotateServiceRequest build() {
            return new RotateServiceRequest(this);
        }
    }

}