package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.ResizeEffectRequest;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class EffectResizedCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;

    private String effectId;
    private TimelinePosition globalPosition;
    private boolean left;

    private TimelinePosition originalPosition;

    private boolean revertable;
    private boolean moreResizeExpected;

    private boolean useSpecialPoints;
    private TimelineLength maximumJumpLength;
    private TimelineLength minimumLength;

    private boolean allowResizeToDisplaceOtherEffects;

    @Generated("SparkTools")
    private EffectResizedCommand(Builder builder) {
        this.timelineManager = builder.timelineManager;
        this.effectId = builder.effectId;
        this.globalPosition = builder.globalPosition;
        this.left = builder.left;
        this.originalPosition = builder.originalPosition;
        this.revertable = builder.revertable;
        this.moreResizeExpected = builder.moreResizeExpected;
        this.useSpecialPoints = builder.useSpecialPoints;
        this.maximumJumpLength = builder.maximumJumpLength;
        this.minimumLength = builder.minimumLength;
        this.allowResizeToDisplaceOtherEffects = builder.allowResizeToDisplaceOtherEffects;
    }

    @Override
    public void execute() {
        StatelessEffect effect = timelineManager.findEffectById(effectId).orElseThrow(() -> new IllegalArgumentException("No effect found"));

        ResizeEffectRequest request = ResizeEffectRequest.builder()
                .withEffect(effect)
                .withLeft(left)
                .withGlobalPosition(globalPosition)
                .withUseSpecialPoints(useSpecialPoints)
                .withMoreResizeExpected(moreResizeExpected)
                .withMaximumJumpLength(maximumJumpLength)
                .withMinimumLength(minimumLength)
                .withAllowResizeToDisplaceOtherEffects(allowResizeToDisplaceOtherEffects)
                .build();

        timelineManager.resizeEffect(request);
    }

    @Override
    public void revert() {
        StatelessEffect effect = timelineManager.findEffectById(effectId).orElseThrow(() -> new IllegalArgumentException("No effect found"));
        TimelinePosition previousPosition = originalPosition;

        ResizeEffectRequest request = ResizeEffectRequest.builder()
                .withEffect(effect)
                .withLeft(left)
                .withGlobalPosition(previousPosition)
                .withUseSpecialPoints(false)
                .withAllowResizeToDisplaceOtherEffects(allowResizeToDisplaceOtherEffects)
                .build();

        timelineManager.resizeEffect(request);
    }

    @Override
    public boolean isRevertable() {
        return revertable;
    }

    @Override
    public String toString() {
        return "EffectResizedCommand [timelineManager=" + timelineManager + ", effectId=" + effectId + ", globalPosition=" + globalPosition + ", left=" + left + ", originalPosition="
                + originalPosition + ", revertable=" + revertable + ", moreResizeExpected=" + moreResizeExpected + ", useSpecialPoints=" + useSpecialPoints + ", maximumJumpLength=" + maximumJumpLength
                + ", minimumLength=" + minimumLength + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineManagerAccessor timelineManager;
        private String effectId;
        private TimelinePosition globalPosition;
        private boolean left;
        private TimelinePosition originalPosition;
        private boolean revertable;
        private boolean moreResizeExpected;
        private boolean useSpecialPoints;
        private TimelineLength maximumJumpLength;
        private TimelineLength minimumLength;
        private boolean allowResizeToDisplaceOtherEffects = false;

        private Builder() {
        }

        public Builder withTimelineManager(TimelineManagerAccessor timelineManager) {
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

        public Builder withOriginalPosition(TimelinePosition originalPosition) {
            this.originalPosition = originalPosition;
            return this;
        }

        public Builder withRevertable(boolean revertable) {
            this.revertable = revertable;
            return this;
        }

        public Builder withMoreResizeExpected(boolean moreResizeExpected) {
            this.moreResizeExpected = moreResizeExpected;
            return this;
        }

        public Builder withUseSpecialPoints(boolean useSpecialPoints) {
            this.useSpecialPoints = useSpecialPoints;
            return this;
        }

        public Builder withMaximumJumpLength(TimelineLength maximumJumpLength) {
            this.maximumJumpLength = maximumJumpLength;
            return this;
        }

        public Builder withMinimumLength(TimelineLength minimumLength) {
            this.minimumLength = minimumLength;
            return this;
        }

        public Builder withAllowResizeToDisplaceOtherEffects(boolean allowResizeToDisplaceOtherEffects) {
            this.allowResizeToDisplaceOtherEffects = allowResizeToDisplaceOtherEffects;
            return this;
        }

        public EffectResizedCommand build() {
            return new EffectResizedCommand(this);
        }
    }
}
