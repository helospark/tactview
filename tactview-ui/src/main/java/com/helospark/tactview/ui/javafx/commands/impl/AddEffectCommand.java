package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class AddEffectCommand implements UiCommand {
    private final String clipId;
    private final String effectId;
    private final TimelinePosition position;

    private TimelineManager timelineManager;

    private String addedEffectId;

    public AddEffectCommand(String clipId, String effectId, TimelinePosition position, TimelineManager timelineManager) {
        this.clipId = clipId;
        this.effectId = effectId;
        this.position = position;
        this.timelineManager = timelineManager;
    }

    @Override
    public void execute() {
        StatelessEffect result = timelineManager.addEffectForClip(clipId, effectId, position);
        addedEffectId = result.getId();
    }

    @Override
    public void revert() {
        timelineManager.removeEffect(addedEffectId);
    }

    public String getAddedEffectId() {
        return addedEffectId;
    }

}
