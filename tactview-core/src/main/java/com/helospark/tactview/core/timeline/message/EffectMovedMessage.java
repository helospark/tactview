package com.helospark.tactview.core.timeline.message;

import java.util.List;
import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class EffectMovedMessage implements AffectedModifiedIntervalAware {
    private String effectId;

    private String originalClipId;

    private TimelinePosition oldPosition;
    private TimelinePosition newPosition;

    private int oldChannelIndex;
    private int newChannelIndex;

    private TimelineInterval originalInterval;
    private TimelineInterval newInterval;

    private Optional<ClosesIntervalChannel> specialPositionUsed;

    @Generated("SparkTools")
    private EffectMovedMessage(Builder builder) {
        this.effectId = builder.effectId;
        this.originalClipId = builder.originalClipId;
        this.oldPosition = builder.oldPosition;
        this.newPosition = builder.newPosition;
        this.oldChannelIndex = builder.oldChannelIndex;
        this.newChannelIndex = builder.newChannelIndex;
        this.originalInterval = builder.originalInterval;
        this.newInterval = builder.newInterval;
        this.specialPositionUsed = builder.specialPositionUsed;
    }

    public String getEffectId() {
        return effectId;
    }

    public String getOriginalClipId() {
        return originalClipId;
    }

    public TimelinePosition getOldPosition() {
        return oldPosition;
    }

    public TimelinePosition getNewPosition() {
        return newPosition;
    }

    public int getOldChannelIndex() {
        return oldChannelIndex;
    }

    public int getNewChannelIndex() {
        return newChannelIndex;
    }

    public Optional<ClosesIntervalChannel> getSpecialPositionUsed() {
        return specialPositionUsed;
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(originalInterval, newInterval);
    }

    @Override
    public String toString() {
        return "EffectMovedMessage [effectId=" + effectId + ", originalClipId=" + originalClipId + ", oldPosition=" + oldPosition + ", newPosition=" + newPosition + ", oldChannelIndex="
                + oldChannelIndex + ", newChannelIndex=" + newChannelIndex + ", originalInterval=" + originalInterval + ", newInterval=" + newInterval + ", specialPositionUsed=" + specialPositionUsed
                + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String effectId;
        private String originalClipId;
        private TimelinePosition oldPosition;
        private TimelinePosition newPosition;
        private int oldChannelIndex;
        private int newChannelIndex;
        private TimelineInterval originalInterval;
        private TimelineInterval newInterval;
        private Optional<ClosesIntervalChannel> specialPositionUsed = Optional.empty();

        private Builder() {
        }

        public Builder withEffectId(String effectId) {
            this.effectId = effectId;
            return this;
        }

        public Builder withOriginalClipId(String originalClipId) {
            this.originalClipId = originalClipId;
            return this;
        }

        public Builder withOldPosition(TimelinePosition oldPosition) {
            this.oldPosition = oldPosition;
            return this;
        }

        public Builder withNewPosition(TimelinePosition newPosition) {
            this.newPosition = newPosition;
            return this;
        }

        public Builder withOldChannelIndex(int oldChannelIndex) {
            this.oldChannelIndex = oldChannelIndex;
            return this;
        }

        public Builder withNewChannelIndex(int newChannelIndex) {
            this.newChannelIndex = newChannelIndex;
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

        public EffectMovedMessage build() {
            return new EffectMovedMessage(this);
        }
    }
}
