package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.ResizeClipRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ClipResizedCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;

    private String clipId;
    private TimelinePosition position;
    private boolean left;

    private TimelineInterval originalInterval;

    private boolean revertable;

    private boolean useSpecialPoints;
    private boolean moreResizeExpected;
    private TimelineLength maximumJumpLength;

    @Generated("SparkTools")
    private ClipResizedCommand(Builder builder) {
        this.timelineManager = builder.timelineManager;
        this.clipId = builder.clipId;
        this.position = builder.position;
        this.left = builder.left;
        this.originalInterval = builder.originalInterval;
        this.revertable = builder.revertable;
        this.useSpecialPoints = builder.useSpecialPoints;
        this.maximumJumpLength = builder.maximumJumpLength;
        this.moreResizeExpected = builder.moreResizeExpected;
    }

    @Override
    public void execute() {
        TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("No clip found"));
        originalInterval = clip.getInterval();

        ResizeClipRequest request = ResizeClipRequest.builder()
                .withClip(clip)
                .withLeft(left)
                .withUseSpecialPoints(useSpecialPoints)
                .withPosition(position)
                .withMaximumJumpLength(maximumJumpLength)
                .withMoreResizeExpected(moreResizeExpected)
                .build();

        timelineManager.resizeClip(request);
    }

    @Override
    public void revert() {
        TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("No clip found"));
        TimelinePosition previousPosition = (left ? originalInterval.getStartPosition() : originalInterval.getEndPosition());

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
        private TimelineInterval originalInterval;
        private boolean revertable;
        private boolean useSpecialPoints;
        private boolean moreResizeExpected;
        private TimelineLength maximumJumpLength;

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

        public Builder withOriginalInterval(TimelineInterval originalInterval) {
            this.originalInterval = originalInterval;
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

        public ClipResizedCommand build() {
            return new ClipResizedCommand(this);
        }
    }
}
