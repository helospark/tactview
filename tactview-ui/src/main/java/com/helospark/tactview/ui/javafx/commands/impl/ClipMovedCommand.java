package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.MoveClipRequest;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ClipMovedCommand implements UiCommand {
    private boolean isRevertable;

    private String clipId;
    private List<String> additionalClipIds;

    private String originalChannelId;
    private String newChannelId;

    private TimelinePosition newPosition;
    private TimelinePosition previousPosition;

    private TimelineManager timelineManager;

    private boolean enableJumpingToSpecialPosition;
    private TimelineLength maximumJumpLength;

    @Generated("SparkTools")
    private ClipMovedCommand(Builder builder) {
        this.isRevertable = builder.isRevertable;
        this.clipId = builder.clipId;
        this.additionalClipIds = builder.additionalClipIds;
        this.originalChannelId = builder.originalChannelId;
        this.newChannelId = builder.newChannelId;
        this.newPosition = builder.newPosition;
        this.previousPosition = builder.previousPosition;
        this.timelineManager = builder.timelineManager;
        this.enableJumpingToSpecialPosition = builder.enableJumpingToSpecialPosition;
        this.maximumJumpLength = builder.maximumJumpLength;
    }

    @Override
    public void execute() {
        MoveClipRequest request = MoveClipRequest.builder()
                .withClipId(clipId)
                .withAdditionalClipIds(additionalClipIds)
                .withNewPosition(newPosition)
                .withNewChannelId(newChannelId)
                .withMaximumJump(maximumJumpLength)
                .withEnableJumpingToSpecialPosition(enableJumpingToSpecialPosition)
                .build();

        timelineManager.moveClip(request);
    }

    @Override
    public void revert() {
        MoveClipRequest request = MoveClipRequest.builder()
                .withClipId(clipId)
                .withNewPosition(previousPosition)
                .withNewChannelId(originalChannelId)
                .withEnableJumpingToSpecialPosition(false)
                .build();
        timelineManager.moveClip(request);
    }

    @Override
    public boolean isRevertable() {
        return isRevertable;
    }

    @Override
    public String toString() {
        return "ClipMovedCommand [isRevertable=" + isRevertable + ", clipId=" + clipId + ", additionalClipIds=" + additionalClipIds + ", originalChannelId=" + originalChannelId + ", newChannelId="
                + newChannelId + ", newPosition=" + newPosition + ", previousPosition=" + previousPosition + ", timelineManager=" + timelineManager + ", enableJumpingToSpecialPosition="
                + enableJumpingToSpecialPosition + ", maximumJumpLength=" + maximumJumpLength + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private boolean isRevertable;
        private String clipId;
        private List<String> additionalClipIds = Collections.emptyList();
        private String originalChannelId;
        private String newChannelId;
        private TimelinePosition newPosition;
        private TimelinePosition previousPosition;
        private TimelineManager timelineManager;
        private boolean enableJumpingToSpecialPosition;
        private TimelineLength maximumJumpLength;

        private Builder() {
        }

        public Builder withIsRevertable(boolean isRevertable) {
            this.isRevertable = isRevertable;
            return this;
        }

        public Builder withClipId(String clipId) {
            this.clipId = clipId;
            return this;
        }

        public Builder withAdditionalClipIds(List<String> additionalClipIds) {
            this.additionalClipIds = additionalClipIds;
            return this;
        }

        public Builder withOriginalChannelId(String originalChannelId) {
            this.originalChannelId = originalChannelId;
            return this;
        }

        public Builder withNewChannelId(String newChannelId) {
            this.newChannelId = newChannelId;
            return this;
        }

        public Builder withNewPosition(TimelinePosition newPosition) {
            this.newPosition = newPosition;
            return this;
        }

        public Builder withPreviousPosition(TimelinePosition previousPosition) {
            this.previousPosition = previousPosition;
            return this;
        }

        public Builder withTimelineManager(TimelineManager timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public Builder withEnableJumpingToSpecialPosition(boolean enableJumpingToSpecialPosition) {
            this.enableJumpingToSpecialPosition = enableJumpingToSpecialPosition;
            return this;
        }

        public Builder withMaximumJumpLength(TimelineLength maximumJumpLength) {
            this.maximumJumpLength = maximumJumpLength;
            return this;
        }

        public ClipMovedCommand build() {
            return new ClipMovedCommand(this);
        }
    }
}
