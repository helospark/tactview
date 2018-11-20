package com.helospark.tactview.core.timeline.effect.transition;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class ExternalStatelessVideoTransitionEffectRequest {
    private ClipFrameResult firstFrame;
    private ClipFrameResult secondFrame;
    private TimelinePosition globalPosition;
    private double scale;

    @Generated("SparkTools")
    private ExternalStatelessVideoTransitionEffectRequest(Builder builder) {
        this.firstFrame = builder.firstFrame;
        this.secondFrame = builder.secondFrame;
        this.globalPosition = builder.globalPosition;
        this.scale = builder.scale;
    }

    public ClipFrameResult getFirstFrame() {
        return firstFrame;
    }

    public ClipFrameResult getSecondFrame() {
        return secondFrame;
    }

    public TimelinePosition getGlobalPosition() {
        return globalPosition;
    }

    public double getScale() {
        return scale;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ClipFrameResult firstFrame;
        private ClipFrameResult secondFrame;
        private TimelinePosition globalPosition;
        private double scale;

        private Builder() {
        }

        public Builder withFirstFrame(ClipFrameResult firstFrame) {
            this.firstFrame = firstFrame;
            return this;
        }

        public Builder withSecondFrame(ClipFrameResult secondFrame) {
            this.secondFrame = secondFrame;
            return this;
        }

        public Builder withGlobalPosition(TimelinePosition globalPosition) {
            this.globalPosition = globalPosition;
            return this;
        }

        public Builder withScale(double scale) {
            this.scale = scale;
            return this;
        }

        public ExternalStatelessVideoTransitionEffectRequest build() {
            return new ExternalStatelessVideoTransitionEffectRequest(this);
        }
    }

}
