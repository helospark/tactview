package com.helospark.tactview.core.timeline.message;

import java.util.List;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class ClipResizedMessage implements AffectedModifiedIntervalAware {
    private String clipId;
    private TimelineInterval originalInterval;
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

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(originalInterval, newInterval);
    }

}
