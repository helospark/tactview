package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class MoveEffectToChannelCommand implements UiCommand {
    private TimelineManager timelineManager;

    private String effectId;
    private int newChannelIndex;

    private int originalChannelIndex;
    private TimelineClip clip;

    public MoveEffectToChannelCommand(TimelineManager timelineManager, String effectId, int newChannelIndex) {
        this.timelineManager = timelineManager;
        this.effectId = effectId;
        this.newChannelIndex = newChannelIndex;
    }

    @Override
    public void execute() {
        clip = timelineManager.findClipForEffect(effectId).get();
        originalChannelIndex = clip.getEffectChannelIndex(effectId).get();
        timelineManager.moveEffectToChannel(clip, effectId, newChannelIndex);
    }

    @Override
    public void revert() {
        timelineManager.moveEffectToChannel(clip, effectId, originalChannelIndex);
    }

    @Override
    public String toString() {
        return "MoveEffectToChannelCommand [timelineManager=" + timelineManager + ", effectId=" + effectId + ", newChannelIndex=" + newChannelIndex + ", originalChannelIndex=" + originalChannelIndex
                + ", clip=" + clip + "]";
    }

}
