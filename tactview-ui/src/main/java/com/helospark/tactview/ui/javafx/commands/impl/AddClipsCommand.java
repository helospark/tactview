package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class AddClipsCommand implements UiCommand {
    private final String channelId;
    private final TimelinePosition timelinePosition;
    private final String filePath;

    private final TimelineManager timelineManager;

    private String addedClipId = null;

    public AddClipsCommand(String channelId, TimelinePosition timelinePosition, String filePath,
            TimelineManager timelineManager) {
        this.channelId = channelId;
        this.timelinePosition = timelinePosition;
        this.filePath = filePath;

        this.timelineManager = timelineManager;
    }

    @Override
    public void execute() {
        TimelineClip result = timelineManager.addResource(channelId, timelinePosition, filePath);
        this.addedClipId = result.getId();
    }

    @Override
    public void revert() {
        timelineManager.removeResource(addedClipId);
    }

    public String getAddedClipId() {
        return addedClipId;
    }

}
