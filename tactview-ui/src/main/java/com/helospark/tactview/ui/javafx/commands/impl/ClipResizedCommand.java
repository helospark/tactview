package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.ResizeClipRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ClipResizedCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;

    private String clipId;
    private TimelinePosition position;
    private boolean left;

    private TimelinePosition originalPosition;

    private boolean revertable;

    private boolean useSpecialPoints;
    private boolean moreResizeExpected;
    private TimelineLength maximumJumpLength;
    private TimelineLength minimumSize;

    @Generated("SparkTools")
    private ClipResizedCommand(Builder builder) {
        this.timelineManager = builder.timelineManager;
        this.clipId = builder.clipId;
        this.position = builder.position;
        this.left = builder.left;
        this.originalPosition = builder.originalPosition;
        this.revertable = builder.revertable;
        this.useSpecialPoints = builder.useSpecialPoints;
        this.maximumJumpLength = builder.maximumJumpLength;
        this.moreResizeExpected = builder.moreResizeExpected;
        this.minimumSize = builder.minimumSize;
    }

    @Override
    public void execute() {
        TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("No clip found"));

        ResizeClipRequest request = ResizeClipRequest.builder()
                .withClip(clip)
                .withLeft(left)
                .withUseSpecialPoints(useSpecialPoints)
                .withPosition(position)
                .withMaximumJumpLength(maximumJumpLength)
                .withMoreResizeExpected(moreResizeExpected)
                .withMinimumSize(minimumSize)
                .build();

        timelineManager.resizeClip(request);
    }

    @Override
    public void revert() {
        TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("No clip found"));
        TimelinePosition previousPosition = originalPosition;

        ResizeClipRequest request = ResizeClipRequest.builder()
                .withClip(clip)
                .withLeft(left)
                .withUseSpecialPoints(false)
                .withPosition(previousPosition)
                .build();

        timelineManager.resizeClip(request);
    }

    @Override
    public boolean isRevertable() {
        return revertable;
    }

    @Override
    public String toString() {
        return "ClipResizedCommand [timelineManager=" + timelineManager + ", clipId=" + clipId + ", position=" + position + ", left=" + left + ", originalPosition=" + originalPosition
                + ", revertable=" + revertable + ", useSpecialPoints=" + useSpecialPoints + ", moreResizeExpected=" + moreResizeExpected + ", maximumJumpLength=" + maximumJumpLength + ", minimumSize="
                + minimumSize + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineManagerAccessor timelineManager;
        private String clipId;
        private TimelinePosition position;
        private boolean left;
        private TimelinePosition originalPosition;
        private boolean revertable;
        private boolean useSpecialPoints;
        private boolean moreResizeExpected;
        private TimelineLength maximumJumpLength;
        private TimelineLength minimumSize;

        private Builder() {
        }

        public Builder withTimelineManager(TimelineManagerAccessor timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public Builder withClipId(String clipId) {
            this.clipId = clipId;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withLeft(boolean left) {
            this.left = left;
            return this;
        }

        public Builder withOriginalPosition(TimelinePosition originalPosition) {
            this.originalPosition = originalPosition;
            return this;
        }

        public Builder withRevertable(boolean revertable) {
            this.revertable = revertable;
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

        public Builder withMinimumSize(TimelineLength minimumSize) {
            this.minimumSize = minimumSize;
            return this;
        }

        public ClipResizedCommand build() {
            return new ClipResizedCommand(this);
        }
    }
}
