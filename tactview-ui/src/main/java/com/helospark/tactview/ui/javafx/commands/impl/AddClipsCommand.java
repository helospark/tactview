package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class AddClipsCommand implements UiCommand {
    private final AddClipRequest request;

    private final TimelineManager timelineManager;

    private String addedClipId = null;

    public AddClipsCommand(AddClipRequest request,
            TimelineManager timelineManager) {
        this.request = request;

        this.timelineManager = timelineManager;
    }

    @Override
    public void execute() {
        TimelineClip result = timelineManager.addClip(request);
        this.addedClipId = result.getId();
    }

    @Override
    public void revert() {
        timelineManager.removeClip(addedClipId);
    }

    public String getAddedClipId() {
        return addedClipId;
    }

    public TimelinePosition getRequestedPosition() {
        return request.getPosition();
    }

}
