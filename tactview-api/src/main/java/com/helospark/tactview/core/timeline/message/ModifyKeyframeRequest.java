package com.helospark.tactview.core.timeline.message;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class ModifyKeyframeRequest {
    private String descriptorId;
    private TimelinePosition originalTimelinePosition;
    private TimelinePosition newTimelinePosition;
    private Object value;

    private TimelinePosition revertTimelinePosition;
    private Object revertValue;
    private boolean revertable;

    private ModifyKeyframeRequest(Builder builder) {
        this.descriptorId = builder.descriptorId;
        this.originalTimelinePosition = builder.originalTimelinePosition;
        this.newTimelinePosition = builder.newTimelinePosition;
        this.value = builder.value;
        this.revertTimelinePosition = builder.revertTimelinePosition;
        this.revertValue = builder.revertValue;
        this.revertable = builder.revertable;
    }

    public String getDescriptorId() {
        return descriptorId;
    }

    public TimelinePosition getOriginalTimelinePosition() {
        return originalTimelinePosition;
    }

    public TimelinePosition getNewTimelinePosition() {
        return newTimelinePosition;
    }

    public Object getValue() {
        return value;
    }

    public boolean isRevertable() {
        return revertable;
    }

    public TimelinePosition getRevertTimelinePosition() {
        return revertTimelinePosition;
    }

    public Object getRevertValue() {
        return revertValue;
    }

    @Override
    public String toString() {
        return "ModifyKeyframeRequest [descriptorId=" + descriptorId + ", originalTimelinePosition=" + originalTimelinePosition + ", newTimelinePosition=" + newTimelinePosition + ", value=" + value
                + ", revertTimelinePosition=" + revertTimelinePosition + ", revertValue=" + revertValue + ", revertable=" + revertable + "]";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String descriptorId;
        private TimelinePosition originalTimelinePosition;
        private TimelinePosition newTimelinePosition;
        private Object value;
        private TimelinePosition revertTimelinePosition;
        private Object revertValue;
        private boolean revertable;
        private Builder() {
        }

        public Builder withDescriptorId(String descriptorId) {
            this.descriptorId = descriptorId;
            return this;
        }

        public Builder withOriginalTimelinePosition(TimelinePosition originalTimelinePosition) {
            this.originalTimelinePosition = originalTimelinePosition;
            return this;
        }

        public Builder withNewTimelinePosition(TimelinePosition newTimelinePosition) {
            this.newTimelinePosition = newTimelinePosition;
            return this;
        }

        public Builder withValue(Object value) {
            this.value = value;
            return this;
        }

        public Builder withRevertTimelinePosition(TimelinePosition revertTimelinePosition) {
            this.revertTimelinePosition = revertTimelinePosition;
            return this;
        }

        public Builder withRevertValue(Object revertValue) {
            this.revertValue = revertValue;
            return this;
        }

        public Builder withRevertable(boolean revertable) {
            this.revertable = revertable;
            return this;
        }

        public ModifyKeyframeRequest build() {
            return new ModifyKeyframeRequest(this);
        }
    }

}
