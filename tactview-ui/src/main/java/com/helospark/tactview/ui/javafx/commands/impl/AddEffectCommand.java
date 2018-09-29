package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class AddEffectCommand implements UiCommand {
    private final String clipId;
    private final String effectId;
    private final TimelineInterval interval;

    private TimelineManager timelineManager;

    private String addedEffectId;

    public AddEffectCommand(String clipId, String effectId, TimelineInterval interval, TimelineManager timelineManager) {
        this.clipId = clipId;
        this.effectId = effectId;
        this.interval = interval;
        this.timelineManager = timelineManager;
    }

    @Override
    public void execute() {
        StatelessEffect result = timelineManager.addEffectForClip(clipId, effectId, interval);
        addedEffectId = result.getId();
    }

    @Override
    public void revert() {
        //timelineManager.removeEffect(result); // todo later
    }

    public String getAddedEffectId() {
        return addedEffectId;
    }

}
