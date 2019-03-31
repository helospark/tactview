package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ChangeClipForEffectCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;

    private String effectId;
    private String newClipId;
    private TimelinePosition newPosition;

    private StatelessEffect originalEffect;
    private TimelineClip originalContainingClip;
    private TimelinePosition originalPosition;

    public ChangeClipForEffectCommand(TimelineManagerAccessor timelineManager, String effectId, String newClipId, TimelinePosition newPosition) {
        this.timelineManager = timelineManager;
        this.effectId = effectId;
        this.newClipId = newClipId;
        this.newPosition = newPosition;
    }

    @Override
    public void execute() {
        originalEffect = timelineManager.findEffectById(effectId).orElseThrow();
        originalContainingClip = timelineManager.findClipForEffect(effectId).orElseThrow();
        originalPosition = originalEffect.getInterval().getStartPosition();

        timelineManager.changeClipForEffect(originalEffect, newClipId, newPosition);
    }

    @Override
    public void revert() {
        timelineManager.changeClipForEffect(originalEffect, originalContainingClip.getId(), originalPosition);
    }

}
