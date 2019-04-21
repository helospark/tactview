package com.helospark.tactview.core.timeline.message;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class KeyframeAddedRequest {
    private String descriptorId;
    private TimelinePosition globalTimelinePosition;
    private String value;
    private boolean revertable;

    @Generated("SparkTools")
    private KeyframeAddedRequest(Builder builder) {
        this.descriptorId = builder.descriptorId;
        this.globalTimelinePosition = builder.globalTimelinePosition;
        this.value = builder.value;
        this.revertable = builder.revertable;
    }

    public String getDescriptorId() {
        return descriptorId;
    }

    public TimelinePosition getGlobalTimelinePosition() {
        return globalTimelinePosition;
    }

    public String getValue() {
        return value;
    }

    public boolean isRevertable() {
        return revertable;
    }

    @Override
    public String toString() {
        return "KeyframeAddedRequest [descriptorId=" + descriptorId + ", globalTimelinePosition=" + globalTimelinePosition + ", value=" + value + ", revertable=" + revertable + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String descriptorId;
        private TimelinePosition globalTimelinePosition;
        private String value;
        private boolean revertable;

        private Builder() {
        }

        public Builder withDescriptorId(String descriptorId) {
            this.descriptorId = descriptorId;
            return this;
        }

        public Builder withGlobalTimelinePosition(TimelinePosition globalTimelinePosition) {
            this.globalTimelinePosition = globalTimelinePosition;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withRevertable(boolean revertable) {
            this.revertable = revertable;
            return this;
        }

        public KeyframeAddedRequest build() {
            return new KeyframeAddedRequest(this);
        }
    }

}
