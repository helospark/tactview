package com.helospark.tactview.ui.javafx;

import java.util.Collection;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.RemoveEffectCommand;

@Component
public class RemoveEffectService {
    private UiCommandInterpreterService commandInterpreterService;
    private TimelineManager timelineManager;

    public RemoveEffectService(UiCommandInterpreterService commandInterpreterService, TimelineManager timelineManager) {
        this.commandInterpreterService = commandInterpreterService;
        this.timelineManager = timelineManager;
    }

    public void removeEffect(String effectId) {
        commandInterpreterService.sendWithResult(new RemoveEffectCommand(timelineManager, effectId));
    }

    public void removeEffects(Collection<String> effectIds) {
        effectIds.stream()
                .forEach(clipId -> removeEffect(clipId));
    }

}
