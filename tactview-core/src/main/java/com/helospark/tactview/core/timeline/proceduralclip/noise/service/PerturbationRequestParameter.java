package com.helospark.tactview.core.timeline.proceduralclip.noise.service;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class PerturbationRequestParameter {
    private Integer seed;
    private float frequency;
    private float gradientPerturb;
    private double colorScale;
    private boolean isFractal;
    private float zPos;
    private Point startPoint;
    private int width;
    private int height;

    @Generated("SparkTools")
    private PerturbationRequestParameter(Builder builder) {
        this.seed = builder.seed;
        this.frequency = builder.frequency;
        this.gradientPerturb = builder.gradientPerturb;
        this.colorScale = builder.colorScale;
        this.isFractal = builder.isFractal;
        this.zPos = builder.zPos;
        this.startPoint = builder.startPoint;
        this.width = builder.width;
        this.height = builder.height;
    }

    public Integer getSeed() {
        return seed;
    }

    public float getFrequency() {
        return frequency;
    }

    public float getGradientPerturb() {
        return gradientPerturb;
    }

    public double getColorScale() {
        return colorScale;
    }

    public boolean isFractal() {
        return isFractal;
    }

    public float getzPos() {
        return zPos;
    }

    public Point getStartPoint() {
        return startPoint;
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
        private Integer seed;
        private float frequency;
        private float gradientPerturb;
        private double colorScale;
        private boolean isFractal;
        private float zPos;
        private Point startPoint;
        private int width;
        private int height;

        private Builder() {
        }

        public Builder withSeed(Integer seed) {
            this.seed = seed;
            return this;
        }

        public Builder withFrequency(float frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder withGradientPerturb(float gradientPerturb) {
            this.gradientPerturb = gradientPerturb;
            return this;
        }

        public Builder withColorScale(double colorScale) {
            this.colorScale = colorScale;
            return this;
        }

        public Builder withIsFractal(boolean isFractal) {
            this.isFractal = isFractal;
            return this;
        }

        public Builder withZPos(float zPos) {
            this.zPos = zPos;
            return this;
        }

        public Builder withStartPoint(Point startPoint) {
            this.startPoint = startPoint;
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

        public PerturbationRequestParameter build() {
            return new PerturbationRequestParameter(this);
        }
    }
}