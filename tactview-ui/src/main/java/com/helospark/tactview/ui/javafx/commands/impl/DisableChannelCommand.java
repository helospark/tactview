package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class DisableChannelCommand implements UiCommand {
    private TimelineManager timelineManager;
    private String channelId;
    private boolean isDisable;
    private boolean hasChanged;

    public DisableChannelCommand(TimelineManager timelineManager, String channelId, boolean isDisable) {
        this.timelineManager = timelineManager;
        this.isDisable = isDisable;
        this.channelId = channelId;
    }

    @Override
    public void execute() {
        hasChanged = timelineManager.disableChannel(channelId, isDisable);
    }

    @Override
    public void revert() {
        if (hasChanged) {
            timelineManager.disableChannel(channelId, !isDisable);
        }
    }

}
