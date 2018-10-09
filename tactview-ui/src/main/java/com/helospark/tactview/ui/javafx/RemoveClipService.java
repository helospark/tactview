package com.helospark.tactview.ui.javafx;

import java.util.Collection;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.RemoveClipCommand;

@Component
public class RemoveClipService {
    private UiCommandInterpreterService commandInterpreterService;
    private TimelineManager timelineManager;

    public RemoveClipService(UiCommandInterpreterService commandInterpreterService, TimelineManager timelineManager) {
        this.commandInterpreterService = commandInterpreterService;
        this.timelineManager = timelineManager;
    }

    public void removeClip(String clipId) {
        commandInterpreterService.sendWithResult(new RemoveClipCommand(timelineManager, clipId));
    }

    public void removeClips(Collection<String> clipIds) {
        clipIds.stream()
                .forEach(clipId -> removeClip(clipId));
    }

}
