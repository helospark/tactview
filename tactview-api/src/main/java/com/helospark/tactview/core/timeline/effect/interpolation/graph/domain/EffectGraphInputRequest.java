package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class EffectGraphInputRequest {
    public ReadOnlyClipImage input;
    public TimelinePosition position;
    public TimelinePosition relativePosition;
    public double scale;
    public int expectedWidth;
    public int expectedHeight;
    public boolean applyEffects;
    public boolean useApproximatePosition;
    public boolean lowResolutionPreview;

    @Generated("SparkTools")
    private EffectGraphInputRequest(Builder builder) {
        this.input = builder.input;
        this.position = builder.position;
        this.relativePosition = builder.relativePosition;
        this.scale = builder.scale;
        this.expectedWidth = builder.expectedWidth;
        this.expectedHeight = builder.expectedHeight;
        this.applyEffects = builder.applyEffects;
        this.useApproximatePosition = builder.useApproximatePosition;
        this.lowResolutionPreview = builder.lowResolutionPreview;
    }

    @Override
    public String toString() {
        return "EffectGraphInputRequest [input=" + input + ", position=" + position + ", relativePosition=" + relativePosition + ", scale=" + scale + ", expectedWidth=" + expectedWidth
                + ", expectedHeight=" + expectedHeight + ", applyEffects=" + applyEffects + ", useApproximatePosition=" + useApproximatePosition + ", lowResolutionPreview=" + lowResolutionPreview
                + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }
    @Generated("SparkTools")
    public static final class Builder {
        private ReadOnlyClipImage input;
        private TimelinePosition position;
        private TimelinePosition relativePosition;
        private double scale;
        private int expectedWidth;
        private int expectedHeight;
        private boolean applyEffects;
        private boolean useApproximatePosition;
        private boolean lowResolutionPreview;
        private Builder() {
        }

        public Builder withInput(ReadOnlyClipImage input) {
            this.input = input;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withRelativePosition(TimelinePosition relativePosition) {
            this.relativePosition = relativePosition;
            return this;
        }

        public Builder withScale(double scale) {
            this.scale = scale;
            return this;
        }

        public Builder withExpectedWidth(int expectedWidth) {
            this.expectedWidth = expectedWidth;
            return this;
        }

        public Builder withExpectedHeight(int expectedHeight) {
            this.expectedHeight = expectedHeight;
            return this;
        }

        public Builder withApplyEffects(boolean applyEffects) {
            this.applyEffects = applyEffects;
            return this;
        }

        public Builder withUseApproximatePosition(boolean useApproximatePosition) {
            this.useApproximatePosition = useApproximatePosition;
            return this;
        }

        public Builder withLowResolutionPreview(boolean lowResolutionPreview) {
            this.lowResolutionPreview = lowResolutionPreview;
            return this;
        }

        public EffectGraphInputRequest build() {
            return new EffectGraphInputRequest(this);
        }
    }

}
