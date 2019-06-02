package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.MoveClipRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ClipToLeftCommand implements UiCommand {
    private String clipId;
    private List<String> additionalClipIds;
    private String originalChannelId;
    private TimelinePosition previousPosition;
    private TimelineManagerAccessor timelineManager;

    private boolean wasOperationSuccessful = false;

    @Generated("SparkTools")
    private ClipToLeftCommand(Builder builder) {
        this.clipId = builder.clipId;
        this.additionalClipIds = builder.additionalClipIds;
        this.timelineManager = builder.timelineManager;
    }

    @Override
    public void execute() {
        TimelineClip clip = timelineManager.findClipById(clipId).get();
        previousPosition = clip.getInterval().getStartPosition();
        originalChannelId = timelineManager.findChannelForClipId(clipId).get().getId();
        Optional<TimelineClip> firstClipToLeft = timelineManager.findFirstClipToLeft(clipId);
        TimelinePosition positionToMoveTo = firstClipToLeft.map(a -> a.getInterval().getEndPosition()).orElse(TimelinePosition.ofZero());

        MoveClipRequest request = MoveClipRequest.builder()
                .withClipId(clipId)
                .withAdditionalClipIds(additionalClipIds)
                .withNewPosition(positionToMoveTo)
                .withNewChannelId(originalChannelId)
                .withMoreMoveExpected(false)
                .withEnableJumpingToSpecialPosition(false)
                .build();

        wasOperationSuccessful = timelineManager.moveClip(request);
    }

    @Override
    public void revert() {
        if (wasOperationSuccessful) {
            MoveClipRequest request = MoveClipRequest.builder()
                    .withClipId(clipId)
                    .withAdditionalClipIds(additionalClipIds)
                    .withNewPosition(previousPosition)
                    .withNewChannelId(originalChannelId)
                    .withEnableJumpingToSpecialPosition(false)
                    .build();
            timelineManager.moveClip(request);
        }
    }

    public boolean wasOperationSuccessful() {
        return wasOperationSuccessful;
    }

    @Override
    public String toString() {
        return "ClipToLeftCommand [clipId=" + clipId + ", additionalClipIds=" + additionalClipIds + ", originalChannelId=" + originalChannelId + ", previousPosition=" + previousPosition
                + ", timelineManager=" + timelineManager + ", wasOperationSuccessful=" + wasOperationSuccessful + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String clipId;
        private List<String> additionalClipIds = Collections.emptyList();
        private TimelineManagerAccessor timelineManager;

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

        public Builder withTimelineManager(TimelineManagerAccessor timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public ClipToLeftCommand build() {
            return new ClipToLeftCommand(this);
        }
    }
}
