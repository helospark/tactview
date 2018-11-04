package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class RemoveEffectCommand implements UiCommand {
    private TimelineManager timelineManager;

    private String effectId;

    private TimelineClip removedFromClip;
    private StatelessEffect removedEffect;

    public RemoveEffectCommand(TimelineManager timelineManager, String effectId) {
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

}
