package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class RemoveEffectCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;

    private String effectId;

    private TimelineClip removedFromClip;
    private StatelessEffect removedEffect;

    public RemoveEffectCommand(TimelineManagerAccessor timelineManager, String effectId) {
        this.timelineManager = timelineManager;
        this.effectId = effectId;
    }

    @Override
    public void execute() {
        removedFromClip = timelineManager.findClipForEffect(effectId).orElseThrow();
        removedEffect = timelineManager.findEffectById(effectId).orElseThrow();
        timelineManager.removeEffect(effectId);
    }

    @Override
    public void revert() {
        timelineManager.addEffectForClip(removedFromClip, removedEffect);
    }

    @Override
    public void preDestroy() {
        removedEffect.preDestroy();
    }

}
