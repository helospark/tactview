package com.helospark.tactview.core.timeline;

import java.util.Optional;

import javax.annotation.Generated;

public class ResizeClipRequest {
    private TimelineClip clip;
    private boolean left;
    private TimelinePosition position;
    private TimelineLength maximumJumpLength;
    private TimelineLength minimumSize;
    private boolean useSpecialPoints;
    private boolean moreResizeExpected;

    public TimelineClip getClip() {
        return clip;
    }

    public boolean isLeft() {
        return left;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public TimelineLength getMaximumJumpLength() {
        return maximumJumpLength;
    }

    public boolean isUseSpecialPoints() {
        return useSpecialPoints;
    }

    public Optional<TimelineLength> getMinimumSize() {
        return Optional.ofNullable(minimumSize);
    }

    public boolean isMoreResizeExpected() {
        return moreResizeExpected;
    }

    @Generated("SparkTools")
    private ResizeClipRequest(Builder builder) {
        this.clip = builder.clip;
        this.left = builder.left;
        this.position = builder.position;
        this.maximumJumpLength = builder.maximumJumpLength;
        this.useSpecialPoints = builder.useSpecialPoints;
        this.moreResizeExpected = builder.moreResizeExpected;
        this.minimumSize = builder.minimumSize;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineClip clip;
        private boolean left;
        private TimelinePosition position;
        private TimelineLength maximumJumpLength;
        private boolean useSpecialPoints;
        private boolean moreResizeExpected;
        private TimelineLength minimumSize;

        private Builder() {
        }

        public Builder withClip(TimelineClip clip) {
            this.clip = clip;
            return this;
        }

        public Builder withLeft(boolean left) {
            this.left = left;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withMaximumJumpLength(TimelineLength maximumJumpLength) {
            this.maximumJumpLength = maximumJumpLength;
            return this;
        }

        public Builder withMinimumSize(TimelineLength minimumSize) {
            this.minimumSize = minimumSize;
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

        public ResizeClipRequest build() {
            return new ResizeClipRequest(this);
        }
    }

}
