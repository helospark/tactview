package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.List;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.ResizeClipRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ClipResizedCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;

    private List<String> clipIds;
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
        this.clipIds = builder.clipIds;
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
        TimelinePosition newPosition = null;
        for (int i = 0; i < clipIds.size(); ++i) {
            String clipId = clipIds.get(i);
            TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("No clip found"));

            ResizeClipRequest request = ResizeClipRequest.builder()
                    .withClip(clip)
                    .withLeft(left)
                    .withUseSpecialPoints(i == 0 && useSpecialPoints)
                    .withPosition(i == 0 ? position : newPosition)
                    .withMaximumJumpLength(maximumJumpLength)
                    .withMoreResizeExpected(moreResizeExpected)
                    .withMinimumSize(minimumSize)
                    .withOtherClipsResized(clipIds)
                    .build();

            timelineManager.resizeClip(request);

            if (i == 0) {
                newPosition = left ? clip.getUnmodifiedInterval().getStartPosition() : clip.getUnmodifiedInterval().getEndPosition();
            }
        }
    }

    @Override
    public void revert() {
        for (var clipId : clipIds) {
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
    }

    @Override
    public boolean isRevertable() {
        return revertable;
    }

    @Override
    public String toString() {
        return "ClipResizedCommand [timelineManager=" + timelineManager + ", clipIds=" + clipIds + ", position=" + position + ", left=" + left + ", originalPosition=" + originalPosition
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
        private List<String> clipIds;
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

        public Builder withClipIds(List<String> clipIds) {
            this.clipIds = clipIds;
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
