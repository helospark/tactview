package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class EffectMovedCommand implements UiCommand {
    private String effectId;

    private String originalClipId;
    private String newClipId;

    private TimelinePosition originalPosition;
    private TimelinePosition newPosition;

    private boolean revertable;

    private TimelineManager timelineManager;

    @Generated("SparkTools")
    private EffectMovedCommand(Builder builder) {
        this.effectId = builder.effectId;
        this.originalClipId = builder.originalClipId;
        this.newClipId = builder.newClipId;
        this.originalPosition = builder.originalPosition;
        this.newPosition = builder.newPosition;
        this.revertable = builder.revertable;
        this.timelineManager = builder.timelineManager;
    }

    @Override
    public void execute() {
        timelineManager.moveEffect(effectId, newPosition, newClipId, 0); // todo: channels
    }

    @Override
    public void revert() {
        timelineManager.moveEffect(effectId, originalPosition, originalClipId, 0);
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
        private String effectId;
        private String originalClipId;
        private String newClipId;
        private TimelinePosition originalPosition;
        private TimelinePosition newPosition;
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

        public Builder withNewClipId(String newClipId) {
            this.newClipId = newClipId;
            return this;
        }

        public Builder withOriginalPosition(TimelinePosition originalPosition) {
            this.originalPosition = originalPosition;
            return this;
        }

        public Builder withNewPosition(TimelinePosition newPosition) {
            this.newPosition = newPosition;
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
