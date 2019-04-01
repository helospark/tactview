package com.helospark.tactview.core.timeline.effect.displacementmap.service;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class ApplyDisplacementMapRequest {
    private ReadOnlyClipImage currentFrame;
    private ReadOnlyClipImage displacementMap;
    private double verticalMultiplier;
    private double horizontalMultiplier;

    @Generated("SparkTools")
    private ApplyDisplacementMapRequest(Builder builder) {
        this.currentFrame = builder.currentFrame;
        this.displacementMap = builder.displacementMap;
        this.verticalMultiplier = builder.verticalMultiplier;
        this.horizontalMultiplier = builder.horizontalMultiplier;
    }

    public ReadOnlyClipImage getCurrentFrame() {
        return currentFrame;
    }

    public ReadOnlyClipImage getDisplacementMap() {
        return displacementMap;
    }

    public double getVerticalMultiplier() {
        return verticalMultiplier;
    }

    public double getHorizontalMultiplier() {
        return horizontalMultiplier;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ReadOnlyClipImage currentFrame;
        private ReadOnlyClipImage displacementMap;
        private double verticalMultiplier;
        private double horizontalMultiplier;

        private Builder() {
        }

        public Builder withCurrentFrame(ReadOnlyClipImage currentFrame) {
            this.currentFrame = currentFrame;
            return this;
        }

        public Builder withDisplacementMap(ReadOnlyClipImage displacementMap) {
            this.displacementMap = displacementMap;
            return this;
        }

        public Builder withVerticalMultiplier(double verticalMultiplier) {
            this.verticalMultiplier = verticalMultiplier;
            return this;
        }

        public Builder withHorizontalMultiplier(double horizontalMultiplier) {
            this.horizontalMultiplier = horizontalMultiplier;
            return this;
        }

        public ApplyDisplacementMapRequest build() {
            return new ApplyDisplacementMapRequest(this);
        }
    }
}