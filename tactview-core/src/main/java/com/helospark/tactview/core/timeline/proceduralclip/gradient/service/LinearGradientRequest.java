package com.helospark.tactview.core.timeline.proceduralclip.gradient.service;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;

public class LinearGradientRequest {
    private int width;
    private int height;
    private InterpolationLine normalizedLine;
    private Color startColor;
    private Color endColor;
    private boolean saturateOnEndSide;

    @Generated("SparkTools")
    private LinearGradientRequest(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.normalizedLine = builder.normalizedLine;
        this.startColor = builder.startColor;
        this.endColor = builder.endColor;
        this.saturateOnEndSide = builder.saturateOnEndSide;
    }

    public InterpolationLine getNormalizedLine() {
        return normalizedLine;
    }

    public Color getStartColor() {
        return startColor;
    }

    public Color getEndColor() {
        return endColor;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean getSaturateOnEndSide() {
        return saturateOnEndSide;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private int width;
        private int height;
        private InterpolationLine normalizedLine;
        private Color startColor;
        private Color endColor;
        private boolean saturateOnEndSide;

        private Builder() {
        }

        public Builder withWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder withNormalizedLine(InterpolationLine normalizedLine) {
            this.normalizedLine = normalizedLine;
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

        public Builder withSaturateOnEndSide(boolean saturateOnEndSide) {
            this.saturateOnEndSide = saturateOnEndSide;
            return this;
        }

        public LinearGradientRequest build() {
            return new LinearGradientRequest(this);
        }
    }

}
