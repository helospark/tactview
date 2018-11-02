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
    private TimelinePosition globalNewPosition;

    private boolean revertable;

    private TimelineManager timelineManager;

    @Generated("SparkTools")
    private EffectMovedCommand(Builder builder) {
        this.effectId = builder.effectId;
        this.originalClipId = builder.originalClipId;
        this.newClipId = builder.newClipId;
        this.originalPosition = builder.originalPosition;
        this.globalNewPosition = builder.globalNewPosition;
        this.revertable = builder.revertable;
        this.timelineManager = builder.timelineManager;
    }

    @Override
    public void execute() {
        timelineManager.moveEffect(effectId, globalNewPosition, newClipId);
    }

    @Override
    public void revert() {
        timelineManager.moveEffect(effectId, originalPosition, originalClipId);
    }

    @Override
    public boolean isRevertable() {
        return revertable;
    }

    @Override
    public String toString() {
        return "EffectMovedCommand [effectId=" + effectId + ", originalClipId=" + originalClipId + ", newClipId=" + newClipId + ", originalPosition=" + originalPosition + ", globalNewPosition=" + globalNewPosition + ", revertable=" + revertable
                + ", timelineManager=" + timelineManager + "]";
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
        private TimelinePosition globalNewPosition;
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

        public Builder withGlobalNewPosition(TimelinePosition globalNewPosition) {
            this.globalNewPosition = globalNewPosition;
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
