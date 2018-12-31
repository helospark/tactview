package com.helospark.tactview.core.timeline;

import javax.annotation.Generated;

public class ResizeEffectRequest {
    private StatelessEffect effect;
    private boolean left;
    private TimelinePosition globalPosition;
    private boolean useSpecialPoints;
    private TimelineLength maximumJumpLength;

    @Generated("SparkTools")
    private ResizeEffectRequest(Builder builder) {
        this.effect = builder.effect;
        this.left = builder.left;
        this.globalPosition = builder.globalPosition;
        this.useSpecialPoints = builder.useSpecialPoints;
        this.maximumJumpLength = builder.maximumJumpLength;
    }

    public StatelessEffect getEffect() {
        return effect;
    }

    public boolean isLeft() {
        return left;
    }

    public TimelinePosition getGlobalPosition() {
        return globalPosition;
    }

    public boolean isUseSpecialPoints() {
        return useSpecialPoints;
    }

    public TimelineLength getMaximumJumpLength() {
        return maximumJumpLength;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private StatelessEffect effect;
        private boolean left;
        private TimelinePosition globalPosition;
        private boolean useSpecialPoints;
        private TimelineLength maximumJumpLength;

        private Builder() {
        }

        public Builder withEffect(StatelessEffect effect) {
            this.effect = effect;
            return this;
        }

        public Builder withLeft(boolean left) {
            this.left = left;
            return this;
        }

        public Builder withGlobalPosition(TimelinePosition globalPosition) {
            this.globalPosition = globalPosition;
            return this;
        }

        public Builder withUseSpecialPoints(boolean useSpecialPoints) {
            this.useSpecialPoints = useSpecialPoints;
            return this;
        }

        public Builder withMaximumJumpLength(TimelineLength maximumJumpLength) {
            this.maximumJumpLength = maximumJumpLength;
            return this;
        }

        public ResizeEffectRequest build() {
            return new ResizeEffectRequest(this);
        }
    }
}
