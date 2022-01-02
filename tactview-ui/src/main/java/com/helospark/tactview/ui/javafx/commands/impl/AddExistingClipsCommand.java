package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.AddExistingClipRequest;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class AddExistingClipsCommand implements UiCommand {
    private final AddExistingClipRequest request;
    private final TimelineManagerAccessor timelineManager;

    private boolean success = false;

    public AddExistingClipsCommand(AddExistingClipRequest request,
            TimelineManagerAccessor timelineManager) {
        this.request = request;
        this.timelineManager = timelineManager;
    }

    @Override
    public void execute() {
        success = timelineManager.addExistingClip(request);
    }

    @Override
    public void revert() {
        if (success) {
            timelineManager.removeClip(request.getClipToAdd().getId());
        }
    }

    public boolean isSuccess() {
        return success;
    }
}
