package com.helospark.tactview.core.util.messaging;

import java.util.List;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineInterval;

public class EffectMovedToDifferentClipMessage implements AffectedModifiedIntervalAware {
    private String effectId;
    private String originalClipId;
    private String newClipId;
    private TimelineInterval modifiedInterval;

    @Generated("SparkTools")
    private EffectMovedToDifferentClipMessage(Builder builder) {
        this.effectId = builder.effectId;
        this.originalClipId = builder.originalClipId;
        this.newClipId = builder.newClipId;
        this.modifiedInterval = builder.modifiedInterval;
    }

    public String getEffectId() {
        return effectId;
    }

    public String getOriginalClipId() {
        return originalClipId;
    }

    public String getNewClipId() {
        return newClipId;
    }

    public TimelineInterval getModifiedInterval() {
        return modifiedInterval;
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(modifiedInterval);
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String effectId;
        private String originalClipId;
        private String newClipId;
        private TimelineInterval modifiedInterval;

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

        public Builder withNewClipId(String newClipId) {
            this.newClipId = newClipId;
            return this;
        }

        public Builder withModifiedInterval(TimelineInterval modifiedInterval) {
            this.modifiedInterval = modifiedInterval;
            return this;
        }

        public EffectMovedToDifferentClipMessage build() {
            return new EffectMovedToDifferentClipMessage(this);
        }
    }
}
