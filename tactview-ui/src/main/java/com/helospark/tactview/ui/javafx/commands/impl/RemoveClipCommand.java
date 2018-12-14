package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class RemoveClipCommand implements UiCommand {
    private TimelineManager timelineManager;

    private String clipId;

    private TimelineChannel removedFromChannel;
    private TimelineClip removedClip;

    public RemoveClipCommand(TimelineManager timelineManager, String clipId) {
        this.timelineManager = timelineManager;
        this.clipId = clipId;
    }

    @Override
    public void execute() {
        removedFromChannel = timelineManager.findChannelForClipId(clipId).orElseThrow();
        removedClip = timelineManager.findClipById(clipId).orElseThrow();
        timelineManager.removeClip(clipId);
    }

    @Override
    public void revert() {
        timelineManager.addClip(removedFromChannel, removedClip);
    }

}
