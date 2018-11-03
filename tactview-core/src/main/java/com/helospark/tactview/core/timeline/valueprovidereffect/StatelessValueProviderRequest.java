package com.helospark.tactview.core.timeline.valueprovidereffect;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class StatelessValueProviderRequest {
    private TimelinePosition effectPosition;
    private TimelinePosition clipPosition;

    @Generated("SparkTools")
    private StatelessValueProviderRequest(Builder builder) {
        this.effectPosition = builder.effectPosition;
        this.clipPosition = builder.clipPosition;
    }

    public TimelinePosition getEffectPosition() {
        return effectPosition;
    }

    public TimelinePosition getClipPosition() {
        return clipPosition;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition effectPosition;
        private TimelinePosition clipPosition;

        private Builder() {
        }

        public Builder withEffectPosition(TimelinePosition effectPosition) {
            this.effectPosition = effectPosition;
            return this;
        }

        public Builder withClipPosition(TimelinePosition clipPosition) {
            this.clipPosition = clipPosition;
            return this;
        }

        public StatelessValueProviderRequest build() {
            return new StatelessValueProviderRequest(this);
        }
    }
}
