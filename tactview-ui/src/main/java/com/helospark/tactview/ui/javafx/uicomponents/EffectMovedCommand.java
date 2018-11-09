package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManager;
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

    private TimelineManager timelineManager;

    @Generated("SparkTools")
    private EffectMovedCommand(Builder builder) {
        this.effectId = builder.effectId;
        this.originalClipId = builder.originalClipId;
        this.originalPosition = builder.originalPosition;
        this.globalNewPosition = builder.globalNewPosition;
        this.enableJumpingToSpecialPosition = builder.enableJumpingToSpecialPosition;
        this.maximumJumpLength = builder.maximumJumpLength;
        this.revertable = builder.revertable;
        this.timelineManager = builder.timelineManager;
    }

    @Override
    public void execute() {
        Optional<TimelineLength> jump = Optional.empty();
        if (enableJumpingToSpecialPosition) {
            jump = Optional.ofNullable(maximumJumpLength);
        }
        timelineManager.moveEffect(effectId, globalNewPosition, jump);
    }

    @Override
    public void revert() {
        timelineManager.moveEffect(effectId, originalPosition, Optional.empty());
    }

    @Override
    public boolean isRevertable() {
        return revertable;
    }

    @Override
    public String toString() {
        return "EffectMovedCommand [effectId=" + effectId + ", originalClipId=" + originalClipId + ", originalPosition=" + originalPosition + ", globalNewPosition=" + globalNewPosition
                + ", enableJumpingToSpecialPosition=" + enableJumpingToSpecialPosition + ", maximumJumpLength=" + maximumJumpLength + ", revertable=" + revertable + ", timelineManager="
                + timelineManager + "]";
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
        private TimelineManager timelineManager;

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

        public Builder withTimelineManager(TimelineManager timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public EffectMovedCommand build() {
            return new EffectMovedCommand(this);
        }
    }

}
