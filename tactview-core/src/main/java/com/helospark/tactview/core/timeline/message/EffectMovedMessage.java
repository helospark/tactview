package com.helospark.tactview.core.timeline.message;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class EffectMovedMessage {
    private String effectId;

    private String originalClipId;
    private String newClipId;

    private TimelinePosition oldPosition;
    private TimelinePosition newPosition;

    private int oldChannelIndex;
    private int newChannelIndex;

    @Generated("SparkTools")
    private EffectMovedMessage(Builder builder) {
        this.effectId = builder.effectId;
        this.originalClipId = builder.originalClipId;
        this.newClipId = builder.newClipId;
        this.oldPosition = builder.oldPosition;
        this.newPosition = builder.newPosition;
        this.oldChannelIndex = builder.oldChannelIndex;
        this.newChannelIndex = builder.newChannelIndex;
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

    public TimelinePosition getOldPosition() {
        return oldPosition;
    }

    public TimelinePosition getNewPosition() {
        return newPosition;
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
        private TimelinePosition oldPosition;
        private TimelinePosition newPosition;
        private int oldChannelIndex;
        private int newChannelIndex;

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

        public EffectMovedMessage build() {
            return new EffectMovedMessage(this);
        }
    }

}
