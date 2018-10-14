package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class EffectResizedCommand implements UiCommand {
    private TimelineManager timelineManager;

    private String effectId;
    private TimelinePosition globalPosition;
    private boolean left;

    private TimelineInterval originalInterval;

    private boolean revertable;

    @Generated("SparkTools")
    private EffectResizedCommand(Builder builder) {
        this.timelineManager = builder.timelineManager;
        this.effectId = builder.effectId;
        this.globalPosition = builder.globalPosition;
        this.left = builder.left;
        this.originalInterval = builder.originalInterval;
        this.revertable = builder.revertable;
    }

    @Override
    public void execute() {
        StatelessEffect effect = timelineManager.findEffectById(effectId).orElseThrow(() -> new IllegalArgumentException("No effect found"));
        originalInterval = effect.getInterval();

        timelineManager.resizeEffect(effect, left, globalPosition);
    }

    @Override
    public void revert() {
        StatelessEffect effect = timelineManager.findEffectById(effectId).orElseThrow(() -> new IllegalArgumentException("No effect found"));
        TimelinePosition previousPosition = (left ? originalInterval.getStartPosition() : originalInterval.getEndPosition());
        timelineManager.resizeEffect(effect, left, previousPosition);
    }

    @Override
    public boolean isRevertable() {
        return revertable;
    }

    @Override
    public String toString() {
        return "EffectResizedCommand [timelineManager=" + timelineManager + ", effectId=" + effectId + ", globalPosition=" + globalPosition + ", left=" + left + ", originalInterval=" + originalInterval + ", revertable=" + revertable + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineManager timelineManager;
        private String effectId;
        private TimelinePosition globalPosition;
        private boolean left;
        private TimelineInterval originalInterval;
        private boolean revertable;

        private Builder() {
        }

        public Builder withTimelineManager(TimelineManager timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public Builder withEffectId(String effectId) {
            this.effectId = effectId;
            return this;
        }

        public Builder withGlobalPosition(TimelinePosition globalPosition) {
            this.globalPosition = globalPosition;
            return this;
        }

        public Builder withLeft(boolean left) {
            this.left = left;
            return this;
        }

        public Builder withOriginalInterval(TimelineInterval originalInterval) {
            this.originalInterval = originalInterval;
            return this;
        }

        public Builder withRevertable(boolean revertable) {
            this.revertable = revertable;
            return this;
        }

        public EffectResizedCommand build() {
            return new EffectResizedCommand(this);
        }
    }
}
