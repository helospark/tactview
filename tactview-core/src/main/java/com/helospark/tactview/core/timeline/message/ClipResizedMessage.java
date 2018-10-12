package com.helospark.tactview.core.timeline.message;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineInterval;

public class ClipResizedMessage {
    private String clipId;
    private TimelineInterval newInterval;

    public String getClipId() {
        return clipId;
    }

    public TimelineInterval getNewInterval() {
        return newInterval;
    }

    @Generated("SparkTools")
    private ClipResizedMessage(Builder builder) {
        this.clipId = builder.clipId;
        this.newInterval = builder.newInterval;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String clipId;
        private TimelineInterval newInterval;

        private Builder() {
        }

        public Builder withClipId(String clipId) {
            this.clipId = clipId;
            return this;
        }

        public Builder withNewInterval(TimelineInterval newInterval) {
            this.newInterval = newInterval;
            return this;
        }

        public ClipResizedMessage build() {
            return new ClipResizedMessage(this);
        }
    }

}
