package com.helospark.tactview.core.timeline;

import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

public class MoveClipRequest {
    public String clipId;
    public List<String> additionalClipIds;
    public TimelinePosition newPosition;
    public String newChannelId;
    public boolean enableJumpingToSpecialPosition;
    public boolean moreMoveExpected;
    public TimelineLength maximumJump;

    @Generated("SparkTools")
    private MoveClipRequest(Builder builder) {
        this.clipId = builder.clipId;
        this.additionalClipIds = builder.additionalClipIds;
        this.newPosition = builder.newPosition;
        this.newChannelId = builder.newChannelId;
        this.enableJumpingToSpecialPosition = builder.enableJumpingToSpecialPosition;
        this.maximumJump = builder.maximumJump;
        this.moreMoveExpected = builder.moreMoveExpected;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String clipId;
        private List<String> additionalClipIds = Collections.emptyList();
        private TimelinePosition newPosition;
        private String newChannelId;
        private boolean enableJumpingToSpecialPosition;
        private boolean moreMoveExpected;
        private TimelineLength maximumJump;

        private Builder() {
        }

        public Builder withClipId(String clipId) {
            this.clipId = clipId;
            return this;
        }

        public Builder withAdditionalClipIds(List<String> additionalClipIds) {
            this.additionalClipIds = additionalClipIds;
            return this;
        }

        public Builder withNewPosition(TimelinePosition newPosition) {
            this.newPosition = newPosition;
            return this;
        }

        public Builder withNewChannelId(String newChannelId) {
            this.newChannelId = newChannelId;
            return this;
        }

        public Builder withEnableJumpingToSpecialPosition(boolean enableJumpingToSpecialPosition) {
            this.enableJumpingToSpecialPosition = enableJumpingToSpecialPosition;
            return this;
        }

        public Builder withMoreMoveExpected(boolean moreMoveExpected) {
            this.moreMoveExpected = moreMoveExpected;
            return this;
        }

        public Builder withMaximumJump(TimelineLength maximumJump) {
            this.maximumJump = maximumJump;
            return this;
        }

        public MoveClipRequest build() {
            return new MoveClipRequest(this);
        }
    }
}
