package com.helospark.tactview.ui.javafx;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.commands.impl.CompositeCommand;
import com.helospark.tactview.ui.javafx.commands.impl.RemoveClipCommand;

@Component
public class RemoveClipService {
    private UiCommandInterpreterService commandInterpreterService;
    private TimelineManagerAccessor timelineManager;

    public RemoveClipService(UiCommandInterpreterService commandInterpreterService, TimelineManagerAccessor timelineManager) {
        this.commandInterpreterService = commandInterpreterService;
        this.timelineManager = timelineManager;
    }

    public void removeClip(String clipId) {
        commandInterpreterService.sendWithResult(createCommand(clipId));
    }

    public void removeClips(Collection<String> clipIds) {
        List<RemoveClipCommand> commands = clipIds.stream()
                .map(clipId -> createCommand(clipId))
                .collect(Collectors.toList());
        commandInterpreterService.sendWithResult(new CompositeCommand(commands));
    }

    private RemoveClipCommand createCommand(String clipId) {
        return new RemoveClipCommand(timelineManager, clipId);
    }

}
