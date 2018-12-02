package com.helospark.tactview.core.timeline.effect.transition;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class InternalStatelessVideoTransitionEffectRequest {
    private ClipImage firstFrame;
    private ReadOnlyClipImage secondFrame;
    private TimelinePosition globalPosition;
    private TimelinePosition clipPosition;
    private TimelinePosition effectPosition;
    private double scale;
    private double progress;

    @Generated("SparkTools")
    private InternalStatelessVideoTransitionEffectRequest(Builder builder) {
        this.firstFrame = builder.firstFrame;
        this.secondFrame = builder.secondFrame;
        this.globalPosition = builder.globalPosition;
        this.clipPosition = builder.clipPosition;
        this.effectPosition = builder.effectPosition;
        this.scale = builder.scale;
        this.progress = builder.progress;
    }

    public ClipImage getFirstFrame() {
        return firstFrame;
    }

    public ReadOnlyClipImage getSecondFrame() {
        return secondFrame;
    }

    public TimelinePosition getGlobalPosition() {
        return globalPosition;
    }

    public TimelinePosition getClipPosition() {
        return clipPosition;
    }

    public TimelinePosition getEffectPosition() {
        return effectPosition;
    }

    public double getScale() {
        return scale;
    }

    public double getProgress() {
        return progress;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ClipImage firstFrame;
        private ReadOnlyClipImage secondFrame;
        private TimelinePosition globalPosition;
        private TimelinePosition clipPosition;
        private TimelinePosition effectPosition;
        private double scale;
        private double progress;

        private Builder() {
        }

        public Builder withFirstFrame(ClipImage firstFrame) {
            this.firstFrame = firstFrame;
            return this;
        }

        public Builder withSecondFrame(ReadOnlyClipImage secondFrame) {
            this.secondFrame = secondFrame;
            return this;
        }

        public Builder withGlobalPosition(TimelinePosition globalPosition) {
            this.globalPosition = globalPosition;
            return this;
        }

        public Builder withClipPosition(TimelinePosition clipPosition) {
            this.clipPosition = clipPosition;
            return this;
        }

        public Builder withEffectPosition(TimelinePosition effectPosition) {
            this.effectPosition = effectPosition;
            return this;
        }

        public Builder withScale(double scale) {
            this.scale = scale;
            return this;
        }

        public Builder withProgress(double progress) {
            this.progress = progress;
            return this;
        }

        public InternalStatelessVideoTransitionEffectRequest build() {
            return new InternalStatelessVideoTransitionEffectRequest(this);
        }
    }
}
