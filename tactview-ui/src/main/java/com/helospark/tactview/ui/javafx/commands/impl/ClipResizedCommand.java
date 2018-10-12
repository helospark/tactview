package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ClipResizedCommand implements UiCommand {
    private TimelineManager timelineManager;

    private String clipId;
    private TimelinePosition position;
    private boolean left;

    private TimelineInterval originalInterval;

    private boolean revertable;

    @Generated("SparkTools")
    private ClipResizedCommand(Builder builder) {
        this.timelineManager = builder.timelineManager;
        this.clipId = builder.clipId;
        this.position = builder.position;
        this.left = builder.left;
        this.revertable = builder.revertable;
    }

    @Override
    public void execute() {
        TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("No clip found"));
        originalInterval = clip.getInterval();

        timelineManager.resizeClip(clip, left, position);
    }

    @Override
    public void revert() {
        TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("No clip found"));
        TimelinePosition previousPosition = (left ? originalInterval.getStartPosition() : originalInterval.getEndPosition());
        timelineManager.resizeClip(clip, left, previousPosition);
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
        private TimelineManager timelineManager;
        private String clipId;
        private TimelinePosition position;
        private boolean left;
        private boolean revertable;

        private Builder() {
        }

        public Builder withTimelineManager(TimelineManager timelineManager) {
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

        public Builder withRevertable(boolean revertable) {
            this.revertable = revertable;
            return this;
        }

        public ClipResizedCommand build() {
            return new ClipResizedCommand(this);
        }
    }
}
