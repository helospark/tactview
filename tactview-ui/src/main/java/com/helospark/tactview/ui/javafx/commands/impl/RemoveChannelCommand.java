package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class RemoveChannelCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;
    private String channelId;

    private int originalChannelIndex;
    private TimelineChannel removedChannel;

    public RemoveChannelCommand(TimelineManagerAccessor timelineManager, String channelId) {
        this.timelineManager = timelineManager;
        this.channelId = channelId;
    }

    @Override
    public void execute() {
        originalChannelIndex = timelineManager.findChannelIndexByChannelId(channelId).get();
        removedChannel = timelineManager.removeChannel(channelId);
    }

    @Override
    public void revert() {
        timelineManager.createChannel(originalChannelIndex, removedChannel);
    }

    public String getChannelId() {
        return channelId;
    }

}
