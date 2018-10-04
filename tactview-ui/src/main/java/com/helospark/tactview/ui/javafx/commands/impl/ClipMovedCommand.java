package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ClipMovedCommand implements UiCommand {
    private boolean isRevertable;

    private String clipId;

    private String originalChannelId;
    private String newChannelId;

    private TimelinePosition newPosition;
    private TimelinePosition previousPosition;

    private TimelineManager timelineManager;

    @Generated("SparkTools")
    private ClipMovedCommand(Builder builder) {
        this.isRevertable = builder.isRevertable;
        this.clipId = builder.clipId;
        this.originalChannelId = builder.originalChannelId;
        this.newChannelId = builder.newChannelId;
        this.newPosition = builder.newPosition;
        this.previousPosition = builder.previousPosition;
        this.timelineManager = builder.timelineManager;
    }

    @Override
    public void execute() {
        timelineManager.moveClip(clipId, newPosition, newChannelId);
    }

    @Override
    public void revert() {
        timelineManager.moveClip(clipId, previousPosition, originalChannelId);
    }

    @Override
    public boolean isRevertable() {
        return isRevertable;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private boolean isRevertable;
        private String clipId;
        private String originalChannelId;
        private String newChannelId;
        private TimelinePosition newPosition;
        private TimelinePosition previousPosition;
        private TimelineManager timelineManager;

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

        public ClipMovedCommand build() {
            return new ClipMovedCommand(this);
        }
    }
}
