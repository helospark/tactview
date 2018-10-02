package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ClipMovedCommand implements UiCommand {
    private boolean isRevertable;
    private String clipId;
    private TimelinePosition newPosition;

    private TimelineManager timelineManager;

    public ClipMovedCommand(boolean isRevertable, String clipId, TimelinePosition newPosition, TimelineManager timelineManager) {
        this.isRevertable = isRevertable;
        this.clipId = clipId;
        this.newPosition = newPosition;
        this.timelineManager = timelineManager;
    }

    @Override
    public void execute() {
        timelineManager.moveClip(clipId, newPosition);
    }

    @Override
    public void revert() {

    }

    @Override
    public boolean isRevertable() {
        return isRevertable;
    }
}
