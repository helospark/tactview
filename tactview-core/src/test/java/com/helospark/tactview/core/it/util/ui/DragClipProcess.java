package com.helospark.tactview.core.it.util.ui;

import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.MoveClipRequest;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class DragClipProcess {
    private TimelineManagerAccessor timelineManagerAccessor;
    public String clipId;
    public List<String> additionalClipIds = List.of();
    public TimelinePosition newPosition;
    public String newChannelId = null;
    public boolean enableJumpingToSpecialPosition = true;
    public boolean moreMoveExpected = true;
    public TimelineLength maximumJump = TimelineLength.ofOne();
    public List<TimelinePosition> additionalSpecialPositions = List.of();
    public TimelinePosition cursorPosition;

    @Generated("SparkTools")
    private DragClipProcess(Builder builder) {
        this.additionalClipIds = builder.additionalClipIds;
        this.newPosition = builder.newPosition;
        this.newChannelId = builder.newChannelId;
        this.enableJumpingToSpecialPosition = builder.enableJumpingToSpecialPosition;
        this.moreMoveExpected = builder.moreMoveExpected;
        this.maximumJump = builder.maximumJump;
        this.additionalSpecialPositions = builder.additionalSpecialPositions;
    }

    public DragClipProcess(TimelineManagerAccessor timelineManagerAccessor, String clipId, TimelinePosition cursorPosition) {
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.clipId = clipId;
        this.cursorPosition = cursorPosition;
    }

    public void dragTo(TimelinePosition position) {
        MoveClipRequest moveClipRequest = MoveClipRequest.builder()
                .withAdditionalClipIds(List.of())
                .withAdditionalSpecialPositions(List.of(cursorPosition))
                .withClipId(clipId)
                .withEnableJumpingToSpecialPosition(true)
                .withMaximumJump(TimelineLength.ofOne())
                .withMoreMoveExpected(false)
                .withNewPosition(position)
                .withNewChannelId(timelineManagerAccessor.findChannelForClipId(clipId).get().getId())
                .build();
        timelineManagerAccessor.moveClip(moveClipRequest);
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private List<String> additionalClipIds = Collections.emptyList();
        private TimelinePosition newPosition;
        private String newChannelId;
        private boolean enableJumpingToSpecialPosition;
        private boolean moreMoveExpected;
        private TimelineLength maximumJump;
        private List<TimelinePosition> additionalSpecialPositions = Collections.emptyList();

        private Builder() {
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

        public Builder withAdditionalSpecialPositions(List<TimelinePosition> additionalSpecialPositions) {
            this.additionalSpecialPositions = additionalSpecialPositions;
            return this;
        }

        public DragClipProcess build() {
            return new DragClipProcess(this);
        }
    }

}
