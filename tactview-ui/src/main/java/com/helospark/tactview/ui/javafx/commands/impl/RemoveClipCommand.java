package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class RemoveClipCommand implements UiCommand {
    private TimelineManager timelineManager;

    private String clipId;

    public RemoveClipCommand(TimelineManager timelineManager, String clipId) {
        this.timelineManager = timelineManager;
        this.clipId = clipId;
    }

    @Override
    public void execute() {
        timelineManager.removeResource(clipId);
    }

    @Override
    public void revert() {
        // TODO
    }

}
