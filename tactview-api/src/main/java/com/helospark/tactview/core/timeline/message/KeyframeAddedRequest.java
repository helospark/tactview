package com.helospark.tactview.core.timeline.message;

import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class KeyframeAddedRequest {
    private String descriptorId;
    private TimelinePosition globalTimelinePosition;
    private Object value;
    private boolean revertable;
    private Optional<Object> previousValue;

    @Generated("SparkTools")
    private KeyframeAddedRequest(Builder builder) {
        this.descriptorId = builder.descriptorId;
        this.globalTimelinePosition = builder.globalTimelinePosition;
        this.value = builder.value;
        this.revertable = builder.revertable;
        this.previousValue = builder.previousValue;
    }

    public String getDescriptorId() {
        return descriptorId;
    }

    public TimelinePosition getGlobalTimelinePosition() {
        return globalTimelinePosition;
    }

    public Object getValue() {
        return value;
    }

    public boolean isRevertable() {
        return revertable;
    }

    public Optional<Object> getPreviousValue() {
        return previousValue;
    }

    @Override
    public String toString() {
        return "KeyframeAddedRequest [descriptorId=" + descriptorId + ", globalTimelinePosition=" + globalTimelinePosition + ", value=" + value + ", revertable=" + revertable + ", previousValue="
                + previousValue + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String descriptorId;
        private TimelinePosition globalTimelinePosition;
        private Object value;
        private boolean revertable;
        private Optional<Object> previousValue = Optional.empty();

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

        public Builder withValue(Object value) {
            this.value = value;
            return this;
        }

        public Builder withRevertable(boolean revertable) {
            this.revertable = revertable;
            return this;
        }

        public Builder withPreviousValue(Optional<Object> previousValue) {
            this.previousValue = previousValue;
            return this;
        }

        public KeyframeAddedRequest build() {
            return new KeyframeAddedRequest(this);
        }
    }

}
