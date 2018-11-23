package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.AddExistingClipRequest;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class AddExistingClipsCommand implements UiCommand {
    private final AddExistingClipRequest request;
    private final TimelineManager timelineManager;

    public AddExistingClipsCommand(AddExistingClipRequest request,
            TimelineManager timelineManager) {
        this.request = request;
        this.timelineManager = timelineManager;
    }

    @Override
    public void execute() {
        timelineManager.addExistingClip(request);
    }

    @Override
    public void revert() {
        timelineManager.removeResource(request.getClipToAdd().getId());
    }

}
