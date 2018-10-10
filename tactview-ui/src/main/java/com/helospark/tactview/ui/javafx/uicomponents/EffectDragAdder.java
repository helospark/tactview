package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddEffectCommand;
import com.helospark.tactview.ui.javafx.repository.DragRepository;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

@Component
public class EffectDragAdder {
    private TimelineManager timelineManager;
    private TimelineState timelineState;
    private UiCommandInterpreterService commandInterpreter;
    private DragRepository dragRepository;

    public EffectDragAdder(TimelineManager timelineManager, TimelineState timelineState, UiCommandInterpreterService commandInterpreter, DragRepository dragRepository) {
        this.timelineManager = timelineManager;
        this.timelineState = timelineState;
        this.commandInterpreter = commandInterpreter;
        this.dragRepository = dragRepository;
    }

    public void addEffectDragOnClip(Node effectNode, Group timelineRow, String clipId) {
        effectNode.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            if (db.getString().startsWith("effect") && !draggingEffect()) {
                System.out.println("Adding dragged effect");
                TimelinePosition position = timelineState.pixelsToSeconds(event.getX());
                AddEffectCommand addEffectCommand = new AddEffectCommand(clipId, extractEffectId(db.getString()), position, timelineManager);
                commandInterpreter.sendWithResult(addEffectCommand).thenAccept(result -> {
                    dragRepository.onEffectDragged(new EffectDragInformation(effectNode, clipId, result.getAddedEffectId(), position));
                });
            }
        });

        effectNode.setOnDragExited(event -> {
        });

        effectNode.setOnDragOver(event -> {
            System.out.println("Effect drag over");
            if (draggingEffect()) {
                System.out.println("Dragging effect");
                moveEffect(effectNode, event, false);

                event.acceptTransferModes(TransferMode.LINK);
                event.consume();
            }
        });

        effectNode.setOnDragDropped(event -> {
            if (draggingEffect()) {
                moveEffect(effectNode, event, true);
                dragRepository.clearEffectDrag();
            }
        });

    }

    private void moveEffect(Node effectNode, DragEvent event, boolean revertable) {
        EffectDragInformation draggedEffect = dragRepository.currentEffectDragInformation();
        TimelinePosition position = timelineState.pixelsToSeconds(event.getX());

        EffectMovedCommand command = EffectMovedCommand.builder()
                .withEffectId(draggedEffect.getEffectId())
                .withOriginalClipId(draggedEffect.getClipId())
                .withNewClipId((String) effectNode.getUserData())
                .withNewPosition(position)
                .withRevertable(revertable)
                .withOriginalPosition(draggedEffect.getOriginalPosition())
                .withTimelineManager(timelineManager)
                .build();
        commandInterpreter.sendWithResult(command);
    }

    private boolean draggingEffect() {
        return dragRepository.currentEffectDragInformation() != null;
    }

    private String extractEffectId(String identified) {
        return identified.replaceFirst("effect:", "");
    }

}
