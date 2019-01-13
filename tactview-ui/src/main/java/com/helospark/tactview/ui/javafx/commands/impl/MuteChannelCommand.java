package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class MuteChannelCommand implements UiCommand {
    private TimelineManager timelineManager;
    private String channelId;
    private boolean isMute;
    private boolean hasChanged;

    public MuteChannelCommand(TimelineManager timelineManager, String channelId, boolean isMute) {
        this.timelineManager = timelineManager;
        this.isMute = isMute;
        this.channelId = channelId;
    }

    @Override
    public void execute() {
        hasChanged = timelineManager.muteChannel(channelId, isMute);
    }

    @Override
    public void revert() {
        if (hasChanged) {
            timelineManager.muteChannel(channelId, !isMute);
        }
    }

    @Override
    public String toString() {
        return "MuteChannelCommand [timelineManager=" + timelineManager + ", channelId=" + channelId + ", isMute=" + isMute + ", hasChanged=" + hasChanged + "]";
    }

}
