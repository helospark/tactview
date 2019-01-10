package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class CutClipCommand implements UiCommand {
    private String clipId;
    private TimelinePosition globalTimelinePosition;

    private TimelineManager timelineManager;

    private TimelineClip originalCuttedClip;
    private TimelineChannel originalChannel;
    private List<String> cuttedPartIds;

    @Generated("SparkTools")
    private CutClipCommand(Builder builder) {
        this.clipId = builder.clipId;
        this.globalTimelinePosition = builder.globalTimelinePosition;
        this.timelineManager = builder.timelineManager;
    }

    @Override
    public void execute() {
        originalCuttedClip = timelineManager.findClipById(clipId).orElseThrow().cloneClip(CloneRequestMetadata.fullCopy());
        originalChannel = timelineManager.findChannelForClipId(clipId).orElseThrow();
        cuttedPartIds = this.timelineManager.cutClip(clipId, globalTimelinePosition)
                .stream()
                .map(clip -> clip.getId())
                .collect(Collectors.toList());
    }

    @Override
    public void revert() {
        cuttedPartIds.stream()
                .forEach(clipId -> timelineManager.removeClip(clipId));
        timelineManager.addClip(originalChannel, originalCuttedClip);
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
