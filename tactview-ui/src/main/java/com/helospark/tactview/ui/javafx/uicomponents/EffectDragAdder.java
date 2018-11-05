package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddEffectCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ChangeClipForEffectCommand;
import com.helospark.tactview.ui.javafx.repository.DragRepository;

import javafx.scene.Node;
import javafx.scene.input.Dragboard;

@Component
public class EffectDragAdder {
    private TimelineManager timelineManager;
    private TimelineState timelineState;
    private UiCommandInterpreterService commandInterpreter;
    private DragRepository dragRepository;
    private UiTimeline uiTimeline;

    public EffectDragAdder(TimelineManager timelineManager, TimelineState timelineState, UiCommandInterpreterService commandInterpreter, DragRepository dragRepository) {
        this.timelineManager = timelineManager;
        this.timelineState = timelineState;
        this.commandInterpreter = commandInterpreter;
        this.dragRepository = dragRepository;
    }

    public void addEffectDragOnClip(Node clipPane, String clipId) {
        clipPane.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            if (db.getString() != null && db.getString().startsWith("effect:") && !draggingEffect()) {
                TimelinePosition position = timelineState.pixelsToSeconds(event.getX());
                AddEffectCommand addEffectCommand = new AddEffectCommand(clipId, extractEffectId(db.getString()), position, timelineManager);
                commandInterpreter.sendWithResult(addEffectCommand).thenAccept(result -> {
                    dragRepository.onEffectDragged(new EffectDragInformation(clipPane, clipId, result.getAddedEffectId(), position, 0));
                });
            } else if (draggingEffectWithoutResize()) {
                EffectDragInformation dragInformation = dragRepository.currentEffectDragInformation();
                TimelinePosition position = timelineState.pixelsToSeconds(event.getX() - dragInformation.getAnchorPointX());
                ChangeClipForEffectCommand command = new ChangeClipForEffectCommand(timelineManager, dragInformation.getEffectId(), clipId, position);
                commandInterpreter.sendWithResult(command);
            }
        });

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
