package com.helospark.tactview.core.timeline.message;

import java.util.List;
import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class ClipResizedMessage implements AffectedModifiedIntervalAware {
    private String clipId;
    private TimelineInterval originalInterval;
    private TimelineInterval newInterval;
    private Optional<ClosesIntervalChannel> specialPointUsed;

    @Generated("SparkTools")
    private ClipResizedMessage(Builder builder) {
        this.clipId = builder.clipId;
        this.originalInterval = builder.originalInterval;
        this.newInterval = builder.newInterval;
        this.specialPointUsed = builder.specialPointUsed;
    }

    public String getClipId() {
        return clipId;
    }

    public TimelineInterval getNewInterval() {
        return newInterval;
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(originalInterval, newInterval);
    }

    public Optional<ClosesIntervalChannel> getSpecialPointUsed() {
        return specialPointUsed;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String clipId;
        private TimelineInterval originalInterval;
        private TimelineInterval newInterval;
        private Optional<ClosesIntervalChannel> specialPointUsed = Optional.empty();

        private Builder() {
        }

        public Builder withClipId(String clipId) {
            this.clipId = clipId;
            return this;
        }

        public Builder withOriginalInterval(TimelineInterval originalInterval) {
            this.originalInterval = originalInterval;
            return this;
        }

        public Builder withNewInterval(TimelineInterval newInterval) {
            this.newInterval = newInterval;
            return this;
        }

        public Builder withSpecialPointUsed(Optional<ClosesIntervalChannel> specialPointUsed) {
            this.specialPointUsed = specialPointUsed;
            return this;
        }

        public ClipResizedMessage build() {
            return new ClipResizedMessage(this);
        }
    }

}
