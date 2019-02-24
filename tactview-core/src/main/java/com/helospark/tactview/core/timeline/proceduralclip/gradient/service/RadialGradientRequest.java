package com.helospark.tactview.core.timeline.proceduralclip.gradient.service;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class RadialGradientRequest {
    private Point center;
    private double radius;
    private Color startColor;
    private Color endColor;
    private double innerSaturation;
    private int width;
    private int height;

    @Generated("SparkTools")
    private RadialGradientRequest(Builder builder) {
        this.center = builder.center;
        this.radius = builder.radius;
        this.startColor = builder.startColor;
        this.endColor = builder.endColor;
        this.innerSaturation = builder.innerSaturation;
        this.width = builder.width;
        this.height = builder.height;
    }

    public Point getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public Color getStartColor() {
        return startColor;
    }

    public Color getEndColor() {
        return endColor;
    }

    public double getInnerSaturation() {
        return innerSaturation;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private Point center;
        private double radius;
        private Color startColor;
        private Color endColor;
        private double innerSaturation;
        private int width;
        private int height;

        private Builder() {
        }

        public Builder withCenter(Point center) {
            this.center = center;
            return this;
        }

        public Builder withRadius(double radius) {
            this.radius = radius;
            return this;
        }

        public Builder withStartColor(Color startColor) {
            this.startColor = startColor;
            return this;
        }

        public Builder withEndColor(Color endColor) {
            this.endColor = endColor;
            return this;
        }

        public Builder withInnerSaturation(double innerSaturation) {
            this.innerSaturation = innerSaturation;
            return this;
        }

        public Builder withWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }

        public RadialGradientRequest build() {
            return new RadialGradientRequest(this);
        }
    }

}
