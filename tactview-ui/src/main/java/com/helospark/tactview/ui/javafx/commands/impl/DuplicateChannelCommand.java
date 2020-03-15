package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class DuplicateChannelCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;
    private String channelId;

    private String createdChannelId;

    public DuplicateChannelCommand(TimelineManagerAccessor timelineManager, String channelId) {
        this.timelineManager = timelineManager;
        this.channelId = channelId;
    }

    @Override
    public void execute() {
        TimelineChannel originalChannel = timelineManager.findChannelWithId(channelId).get();
        TimelineChannel clonedChannel = originalChannel.cloneChannel(CloneRequestMetadata.ofDefault());

        createdChannelId = clonedChannel.getId();
        int originalChannelIndex = timelineManager.findChannelIndexByChannelId(channelId).get();

        timelineManager.createChannel(originalChannelIndex + 1, clonedChannel);
    }

    @Override
    public void revert() {
        timelineManager.removeChannel(createdChannelId);
    }

    public String getChannelId() {
        return channelId;
    }

}
