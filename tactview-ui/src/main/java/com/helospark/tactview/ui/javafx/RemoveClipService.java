package com.helospark.tactview.ui.javafx;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.commands.impl.CompositeCommand;
import com.helospark.tactview.ui.javafx.commands.impl.RemoveClipCommand;
import com.helospark.tactview.ui.javafx.commands.impl.RippleRemoveClipCommand;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditMode;

@Component
public class RemoveClipService {
    private UiCommandInterpreterService commandInterpreterService;
    private TimelineManagerAccessor timelineManager;
    private MessagingService messagingService;

    public RemoveClipService(UiCommandInterpreterService commandInterpreterService, TimelineManagerAccessor timelineManager, MessagingService messagingService) {
        this.commandInterpreterService = commandInterpreterService;
        this.timelineManager = timelineManager;
        this.messagingService = messagingService;
    }

    public void removeClip(String clipId) {
        commandInterpreterService.synchronousSend(createCommand(clipId));
    }

    public void removeClips(Collection<String> clipIds) {
        List<RemoveClipCommand> commands = clipIds.stream()
                .filter(clipId -> timelineManager.findClipById(clipId).isPresent())
                .map(clipId -> createCommand(clipId))
                .collect(Collectors.toList());
        if (commands.size() > 0) {
            commandInterpreterService.synchronousSend(new CompositeCommand(commands));
        }
    }

    private RemoveClipCommand createCommand(String clipId) {
        return new RemoveClipCommand(timelineManager, clipId);
    }

    public void rippleDeleteClips(List<String> clipIds, TimelineEditMode timelineEditProperty) {
        if (clipIds.size() > 0) {
            commandInterpreterService.synchronousSend(new RippleRemoveClipCommand(timelineManager, messagingService, clipIds, timelineEditProperty));
        }
    }

}
