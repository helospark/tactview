package com.helospark.tactview.core.timeline;

import java.util.Optional;

import javax.annotation.Generated;

public class ResizeEffectRequest {
    private StatelessEffect effect;
    private boolean left;
    private TimelinePosition globalPosition;
    private boolean useSpecialPoints;
    private boolean moreResizeExpected;
    private TimelineLength maximumJumpLength;
    private TimelineLength minimumLength;

    @Generated("SparkTools")
    private ResizeEffectRequest(Builder builder) {
        this.effect = builder.effect;
        this.left = builder.left;
        this.globalPosition = builder.globalPosition;
        this.useSpecialPoints = builder.useSpecialPoints;
        this.moreResizeExpected = builder.moreResizeExpected;
        this.maximumJumpLength = builder.maximumJumpLength;
        this.minimumLength = builder.minimumLength;
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

    public boolean isMoreResizeExpected() {
        return moreResizeExpected;
    }

    public boolean isUseSpecialPoints() {
        return useSpecialPoints;
    }

    public Optional<TimelineLength> getMinimumLength() {
        return Optional.ofNullable(minimumLength);
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
        private boolean moreResizeExpected;
        private TimelineLength maximumJumpLength;
        private TimelineLength minimumLength;

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

        public Builder withMoreResizeExpected(boolean moreResizeExpected) {
            this.moreResizeExpected = moreResizeExpected;
            return this;
        }

        public Builder withMaximumJumpLength(TimelineLength maximumJumpLength) {
            this.maximumJumpLength = maximumJumpLength;
            return this;
        }

        public Builder withMinimumLength(TimelineLength minimumLength) {
            this.minimumLength = minimumLength;
            return this;
        }

        public ResizeEffectRequest build() {
            return new ResizeEffectRequest(this);
        }
    }
}
