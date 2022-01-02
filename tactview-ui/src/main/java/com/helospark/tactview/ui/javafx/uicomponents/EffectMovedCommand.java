package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.List;
import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.MoveEffectRequest;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class EffectMovedCommand implements UiCommand {
    private String effectId;

    private String originalClipId;

    private TimelinePosition originalPosition;
    private TimelinePosition globalNewPosition;

    private boolean enableJumpingToSpecialPosition;
    private TimelineLength maximumJumpLength;

    private boolean revertable;
    private boolean moreMoveExpected;

    private TimelineManagerAccessor timelineManager;

    private List<TimelinePosition> additionalSpecialPositions;

    @Generated("SparkTools")
    private EffectMovedCommand(Builder builder) {
        this.effectId = builder.effectId;
        this.originalClipId = builder.originalClipId;
        this.originalPosition = builder.originalPosition;
        this.globalNewPosition = builder.globalNewPosition;
        this.enableJumpingToSpecialPosition = builder.enableJumpingToSpecialPosition;
        this.maximumJumpLength = builder.maximumJumpLength;
        this.revertable = builder.revertable;
        this.moreMoveExpected = builder.moreMoveExpected;
        this.timelineManager = builder.timelineManager;
        this.additionalSpecialPositions = builder.additionalSpecialPositions;
    }

    @Override
    public void execute() {
        Optional<TimelineLength> jump = Optional.empty();
        if (enableJumpingToSpecialPosition) {
            jump = Optional.ofNullable(maximumJumpLength);
        }
        MoveEffectRequest request = MoveEffectRequest.builder()
                .withEffectId(effectId)
                .withGlobalNewPosition(globalNewPosition)
                .withMaximumJumpToSpecialPositions(jump)
                .withMoreMoveExpected(moreMoveExpected)
                .withAdditionalSpecialPositions(additionalSpecialPositions)
                .build();

        timelineManager.moveEffect(request);
    }

    @Override
    public void revert() {
        MoveEffectRequest request = MoveEffectRequest.builder()
                .withEffectId(effectId)
                .withGlobalNewPosition(originalPosition)
                .withMaximumJumpToSpecialPositions(Optional.empty())
                .withMoreMoveExpected(false)
                .build();
        timelineManager.moveEffect(request);
    }

    @Override
    public boolean isRevertable() {
        return revertable;
    }

    @Override
    public String toString() {
        return "EffectMovedCommand [effectId=" + effectId + ", originalClipId=" + originalClipId + ", originalPosition=" + originalPosition + ", globalNewPosition="
                + globalNewPosition
                + ", enableJumpingToSpecialPosition=" + enableJumpingToSpecialPosition + ", maximumJumpLength=" + maximumJumpLength + ", revertable=" + revertable
                + ", moreMoveExpected="
                + moreMoveExpected + ", timelineManager=" + timelineManager + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String effectId;
        private String originalClipId;
        private TimelinePosition originalPosition;
        private TimelinePosition globalNewPosition;
        private boolean enableJumpingToSpecialPosition;
        private TimelineLength maximumJumpLength;
        private boolean revertable;
        private boolean moreMoveExpected;
        private TimelineManagerAccessor timelineManager;
        private List<TimelinePosition> additionalSpecialPositions;

        private Builder() {
        }

        public Builder withEffectId(String effectId) {
            this.effectId = effectId;
            return this;
        }

        public Builder withOriginalClipId(String originalClipId) {
            this.originalClipId = originalClipId;
            return this;
        }

        public Builder withOriginalPosition(TimelinePosition originalPosition) {
            this.originalPosition = originalPosition;
            return this;
        }

        public Builder withGlobalNewPosition(TimelinePosition globalNewPosition) {
            this.globalNewPosition = globalNewPosition;
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

        public Builder withRevertable(boolean revertable) {
            this.revertable = revertable;
            return this;
        }

        public Builder withMoreMoveExpected(boolean moreMoveExpected) {
            this.moreMoveExpected = moreMoveExpected;
            return this;
        }

        public Builder withTimelineManager(TimelineManagerAccessor timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public Builder withAdditionalSpecialPositions(List<TimelinePosition> additionalSpecialPositions) {
            this.additionalSpecialPositions = additionalSpecialPositions;
            return this;
        }

        public EffectMovedCommand build() {
            return new EffectMovedCommand(this);
        }
    }

}
