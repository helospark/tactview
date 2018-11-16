package com.helospark.tactview.core.timeline;

import javax.annotation.Generated;

public class AudioRequest {
    private TimelinePosition position;
    private TimelineLength length;
    private boolean applyEffects;

    @Generated("SparkTools")
    private AudioRequest(Builder builder) {
        this.position = builder.position;
        this.length = builder.length;
        this.applyEffects = builder.applyEffects;
    }

    public TimelineLength getLength() {
        return length;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public boolean isApplyEffects() {
        return applyEffects;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition position;
        private TimelineLength length;
        private boolean applyEffects;

        private Builder() {
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withLength(TimelineLength length) {
            this.length = length;
            return this;
        }

        public Builder withApplyEffects(boolean applyEffects) {
            this.applyEffects = applyEffects;
            return this;
        }

        public AudioRequest build() {
            return new AudioRequest(this);
        }
    }

}
