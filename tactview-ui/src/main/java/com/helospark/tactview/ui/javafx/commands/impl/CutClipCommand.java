package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class CutClipCommand implements UiCommand {
    private String clipId;
    private TimelinePosition globalTimelinePosition;

    private TimelineManager timelineManager;

    @Generated("SparkTools")
    private CutClipCommand(Builder builder) {
        this.clipId = builder.clipId;
        this.globalTimelinePosition = builder.globalTimelinePosition;
        this.timelineManager = builder.timelineManager;
    }

    @Override
    public void execute() {
        this.timelineManager.cutClip(clipId, globalTimelinePosition);
    }

    @Override
    public void revert() {
        // TODO: later
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String clipId;
        private TimelinePosition globalTimelinePosition;
        private TimelineManager timelineManager;

        private Builder() {
        }

        public Builder withClipId(String clipId) {
            this.clipId = clipId;
            return this;
        }

        public Builder withGlobalTimelinePosition(TimelinePosition globalTimelinePosition) {
            this.globalTimelinePosition = globalTimelinePosition;
            return this;
        }

        public Builder withTimelineManager(TimelineManager timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public CutClipCommand build() {
            return new CutClipCommand(this);
        }
    }
}
