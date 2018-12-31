package com.helospark.tactview.core.timeline.message;

import java.util.List;
import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class EffectResizedMessage implements AffectedModifiedIntervalAware {
    private String clipId;
    private String effectId;
    private TimelineInterval originalInterval;
    private TimelineInterval newInterval;
    private Optional<ClosesIntervalChannel> specialPositionUsed;

    @Generated("SparkTools")
    private EffectResizedMessage(Builder builder) {
        this.clipId = builder.clipId;
        this.effectId = builder.effectId;
        this.originalInterval = builder.originalInterval;
        this.newInterval = builder.newInterval;
        this.specialPositionUsed = builder.specialPositionUsed;
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

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(originalInterval, newInterval);
    }

    public Optional<ClosesIntervalChannel> getSpecialPositionUsed() {
        return specialPositionUsed;
    }

    @Override
    public String toString() {
        return "EffectResizedMessage [clipId=" + clipId + ", effectId=" + effectId + ", originalInterval=" + originalInterval + ", newInterval=" + newInterval + ", specialPositionUsed="
                + specialPositionUsed + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String clipId;
        private String effectId;
        private TimelineInterval originalInterval;
        private TimelineInterval newInterval;
        private Optional<ClosesIntervalChannel> specialPositionUsed = Optional.empty();

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

        public Builder withOriginalInterval(TimelineInterval originalInterval) {
            this.originalInterval = originalInterval;
            return this;
        }

        public Builder withNewInterval(TimelineInterval newInterval) {
            this.newInterval = newInterval;
            return this;
        }

        public Builder withSpecialPositionUsed(Optional<ClosesIntervalChannel> specialPositionUsed) {
            this.specialPositionUsed = specialPositionUsed;
            return this;
        }

        public EffectResizedMessage build() {
            return new EffectResizedMessage(this);
        }
    }

}
