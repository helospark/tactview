package com.helospark.tactview.core.timeline.message;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class KeyframeRemovedRequest {
    private String descriptorId;
    private TimelinePosition globalTimelinePosition;

    @Generated("SparkTools")
    private KeyframeRemovedRequest(Builder builder) {
        this.descriptorId = builder.descriptorId;
        this.globalTimelinePosition = builder.globalTimelinePosition;
    }

    public String getDescriptorId() {
        return descriptorId;
    }

    public TimelinePosition getGlobalTimelinePosition() {
        return globalTimelinePosition;
    }

    @Override
    public String toString() {
        return "KeyframeRemovedRequest [descriptorId=" + descriptorId + ", globalTimelinePosition=" + globalTimelinePosition + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String descriptorId;
        private TimelinePosition globalTimelinePosition;

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

        public KeyframeRemovedRequest build() {
            return new KeyframeRemovedRequest(this);
        }
    }

}
