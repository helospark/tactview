package com.helospark.tactview.core.timeline.effect.distort;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

//Part of the code is adapted from Gimp: https://github.com/piksels-and-lines-orchestra/gimp/blob/master/plug-ins/common/polar-coords.c
public class PolarOperationRequest {
    private ReadOnlyClipImage currentFrame;
    private double circleDepth;
    private double offsetAngleDepth;
    private boolean mapBackward;
    private boolean toPolar;
    private boolean mapFromTop;

    @Generated("SparkTools")
    private PolarOperationRequest(Builder builder) {
        this.currentFrame = builder.currentFrame;
        this.circleDepth = builder.circleDepth;
        this.offsetAngleDepth = builder.offsetAngleDepth;
        this.mapBackward = builder.mapBackward;
        this.toPolar = builder.toPolar;
        this.mapFromTop = builder.mapFromTop;
    }

    public ReadOnlyClipImage getCurrentFrame() {
        return currentFrame;
    }

    public double getCircleDepth() {
        return circleDepth;
    }

    public double getOffsetAngleDepth() {
        return offsetAngleDepth;
    }

    public boolean isMapBackward() {
        return mapBackward;
    }

    public boolean isToPolar() {
        return toPolar;
    }

    public boolean isMapFromTop() {
        return mapFromTop;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ReadOnlyClipImage currentFrame;
        private double circleDepth;
        private double offsetAngleDepth;
        private boolean mapBackward;
        private boolean toPolar;
        private boolean mapFromTop;

        private Builder() {
        }

        public Builder withCurrentFrame(ReadOnlyClipImage currentFrame) {
            this.currentFrame = currentFrame;
            return this;
        }

        public Builder withCircleDepth(double circleDepth) {
            this.circleDepth = circleDepth;
            return this;
        }

        public Builder withOffsetAngleDepth(double offsetAngleDepth) {
            this.offsetAngleDepth = offsetAngleDepth;
            return this;
        }

        public Builder withMapBackward(boolean mapBackward) {
            this.mapBackward = mapBackward;
            return this;
        }

        public Builder withToPolar(boolean toPolar) {
            this.toPolar = toPolar;
            return this;
        }

        public Builder withMapFromTop(boolean mapFromTop) {
            this.mapFromTop = mapFromTop;
            return this;
        }

        public PolarOperationRequest build() {
            return new PolarOperationRequest(this);
        }
    }
}