package com.helospark.tactview.core.timeline.message;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineInterval;

public class EffectResizedMessage {
    private String clipId;
    private String effectId;
    private TimelineInterval newInterval;

    @Generated("SparkTools")
    private EffectResizedMessage(Builder builder) {
        this.clipId = builder.clipId;
        this.effectId = builder.effectId;
        this.newInterval = builder.newInterval;
    }

    public String getClipId() {
        return clipId;
    }

    public String getEffectId() {
        return effectId;
    }

    public TimelineInterval getNewInterval() {
        return newInterval;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String clipId;
        private String effectId;
        private TimelineInterval newInterval;

        private Builder() {
        }

        public Builder withClipId(String clipId) {
            this.clipId = clipId;
            return this;
        }

        public Builder withEffectId(String effectId) {
            this.effectId = effectId;
            return this;
        }

        public Builder withNewInterval(TimelineInterval newInterval) {
            this.newInterval = newInterval;
            return this;
        }

        public EffectResizedMessage build() {
            return new EffectResizedMessage(this);
        }
    }

    @Override
    public String toString() {
        return "EffectResizedMessage [clipId=" + clipId + ", effectId=" + effectId + ", newInterval=" + newInterval + "]";
    }

}
