package com.helospark.tactview.core.timeline;

import javax.annotation.Generated;

public class AudioRequest {
    private TimelinePosition position;
    private TimelineLength length;

    @Generated("SparkTools")
    private AudioRequest(Builder builder) {
        this.position = builder.position;
        this.length = builder.length;
    }

    public TimelineLength getLength() {
        return length;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition position;
        private TimelineLength length;

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

        public AudioRequest build() {
            return new AudioRequest(this);
        }
    }

}
