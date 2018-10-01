package com.helospark.tactview.core.timeline.effect;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class StatelessEffectRequest {
    private ClipFrameResult currentFrame;
    private TimelinePosition clipPosition;
    private TimelinePosition effectPosition;

    @Generated("SparkTools")
    private StatelessEffectRequest(Builder builder) {
        this.currentFrame = builder.currentFrame;
        this.clipPosition = builder.clipPosition;
        this.effectPosition = builder.effectPosition;
    }

    public ClipFrameResult getCurrentFrame() {
        return currentFrame;
    }

    public TimelinePosition getClipPosition() {
        return clipPosition;
    }

    public TimelinePosition getEffectPosition() {
        return effectPosition;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ClipFrameResult currentFrame;
        private TimelinePosition clipPosition;
        private TimelinePosition effectPosition;

        private Builder() {
        }

        public Builder withCurrentFrame(ClipFrameResult currentFrame) {
            this.currentFrame = currentFrame;
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

        public StatelessEffectRequest build() {
            return new StatelessEffectRequest(this);
        }
    }

}
