package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class CreateChannelCommand implements UiCommand {
    public static final int LAST_INDEX = -1;
    private TimelineManager timelineManager;
    private int index;

    private String channelId;

    public CreateChannelCommand(TimelineManager timelineManager, int index) {
        this.timelineManager = timelineManager;
        this.index = index;
    }

    @Override
    public void execute() {
        TimelineChannel channel = timelineManager.createChannel(index);
        this.channelId = channel.getId();
    }

    @Override
    public void revert() {
        timelineManager.removeChannel(channelId);
    }

    public String getChannelId() {
        return channelId;
    }

}
