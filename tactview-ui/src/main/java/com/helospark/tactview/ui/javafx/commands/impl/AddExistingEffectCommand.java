package com.helospark.tactview.ui.javafx.commands.impl;

import com.helospark.tactview.core.timeline.AddExistingEffectRequest;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class AddExistingEffectCommand implements UiCommand {
    private final AddExistingEffectRequest request;
    private final TimelineManager timelineManager;

    public AddExistingEffectCommand(AddExistingEffectRequest request,
            TimelineManager timelineManager) {
        this.request = request;
        this.timelineManager = timelineManager;
    }

    @Override
    public void execute() {
        timelineManager.addExistingEffect(request);
    }

    @Override
    public void revert() {
        timelineManager.removeClip(request.getClipToAdd().getId());
    }

}
