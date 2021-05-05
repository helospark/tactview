package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddEffectCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ChangeClipForEffectCommand;
import com.helospark.tactview.ui.javafx.repository.DragRepository;

import javafx.scene.input.Dragboard;

@Component
public class EffectDragAdder {
    private TimelineManagerAccessor timelineManager;
    private UiCommandInterpreterService commandInterpreter;
    private DragRepository dragRepository;

    public EffectDragAdder(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreter, DragRepository dragRepository) {
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
        this.dragRepository = dragRepository;
    }

    public boolean addEffectDragOnClip(String clipId, TimelinePosition position, Dragboard db) {
        if (db != null && db.getString() != null && db.getString().startsWith("effect:") && !draggingEffect()) {
            AddEffectCommand addEffectCommand = new AddEffectCommand(clipId, extractEffectId(db.getString()), position, timelineManager);
            commandInterpreter.sendWithResult(addEffectCommand).thenAccept(result -> {
                dragRepository.onEffectDragged(new EffectDragInformation(clipId, result.getAddedEffectId(), position, 0));
            });
            return true;
        } else if (draggingEffectWithoutResize()) {
            EffectDragInformation dragInformation = dragRepository.currentEffectDragInformation();
            ChangeClipForEffectCommand command = new ChangeClipForEffectCommand(timelineManager, dragInformation.getEffectId(), clipId, position);
            commandInterpreter.sendWithResult(command);
            return true;
        }
        return false;

    }

    private boolean draggingEffectWithoutResize() {
        EffectDragInformation dragInfo = dragRepository.currentEffectDragInformation();
        return dragInfo != null && !dragRepository.isResizing();
    }

    private boolean draggingEffect() {
        return dragRepository.currentEffectDragInformation() != null;
    }

    private String extractEffectId(String identified) {
        return identified.replaceFirst("effect:", "");
    }

}
