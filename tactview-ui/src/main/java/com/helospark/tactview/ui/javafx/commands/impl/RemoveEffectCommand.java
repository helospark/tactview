package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class RemoveEffectCommand implements UiCommand {
    private TimelineManager timelineManager;

    private String effectId;

    public RemoveEffectCommand(TimelineManager timelineManager, String effectId) {
        this.timelineManager = timelineManager;
        this.effectId = effectId;
    }

    @Override
    public void execute() {
        //        timelineManager.findEffectById(effectId); todo revert
        timelineManager.removeEffect(effectId);
    }

    @Override
    public void revert() {
        // TODO
    }

}
