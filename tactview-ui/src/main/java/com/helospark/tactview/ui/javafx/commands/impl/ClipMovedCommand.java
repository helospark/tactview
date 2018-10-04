package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class ClipMovedCommand implements UiCommand {
    private boolean isRevertable;
    private String clipId;
    private TimelinePosition newPosition;

    private TimelinePosition previousPosition;

    private TimelineManager timelineManager;

    public ClipMovedCommand(boolean isRevertable, String clipId, TimelinePosition newPosition, TimelinePosition previousPosition, TimelineManager timelineManager) {
        this.isRevertable = isRevertable;
        this.clipId = clipId;
        this.newPosition = newPosition;
        this.timelineManager = timelineManager;
        this.previousPosition = previousPosition;
    }

    @Override
    public void execute() {
        timelineManager.moveClip(clipId, newPosition);
    }

    @Override
    public void revert() {
        timelineManager.moveClip(clipId, previousPosition);
    }

    @Override
    public boolean isRevertable() {
        return isRevertable;
    }
}
