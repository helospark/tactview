package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddEffectCommand;
import com.helospark.tactview.ui.javafx.commands.impl.EffectResizedCommand;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository.DragDirection;

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

    public void addEffectDragOnClip(Node clipPane, String clipId) {
        clipPane.setOnDragEntered(event -> {
            System.out.println("Effect drag entered");
            Dragboard db = event.getDragboard();
            if (db.getString().startsWith("effect:") && !draggingEffect()) {
                System.out.println("Adding dragged effect");
                TimelinePosition position = timelineState.pixelsToSeconds(event.getX());
                AddEffectCommand addEffectCommand = new AddEffectCommand(clipId, extractEffectId(db.getString()), position, timelineManager);
                commandInterpreter.sendWithResult(addEffectCommand).thenAccept(result -> {
                    dragRepository.onEffectDragged(new EffectDragInformation(clipPane, clipId, result.getAddedEffectId(), position));
                });
            }
        });

        clipPane.setOnDragExited(event -> {
        });

        clipPane.setOnDragOver(event -> {
            System.out.println("Effect drag over");
            if (draggingEffect()) {
                System.out.println("Dragging effect");
                if (dragRepository.isResizing()) {
                    resizeEffect(clipPane, event, false);
                } else {
                    moveEffect(clipPane, event, false);
                }

                event.acceptTransferModes(TransferMode.LINK);
                event.consume();
            }
        });

        clipPane.setOnDragDropped(event -> {
            if (draggingEffect()) {
                if (dragRepository.isResizing()) {
                    resizeEffect(clipPane, event, true);
                } else {
                    moveEffect(clipPane, event, true);
                }
                dragRepository.clearEffectDrag();
            }
        });

    }

    private void resizeEffect(Node clipPane, DragEvent event, boolean revertable) {
        EffectDragInformation draggedEffect = dragRepository.currentEffectDragInformation();

        double x = event.getX();

        System.out.println("Move to " + x);

        EffectResizedCommand resizedCommand = EffectResizedCommand.builder()
                .withEffectId(draggedEffect.getEffectId())
                .withLeft(dragRepository.getDragDirection().equals(DragDirection.LEFT))
                .withGlobalPosition(timelineState.pixelsToSeconds(x))
                .withRevertable(revertable)
                .withTimelineManager(timelineManager)
                .build();

        commandInterpreter.sendWithResult(resizedCommand);
    }

    private void moveEffect(Node effectNode, DragEvent event, boolean revertable) {
        EffectDragInformation draggedEffect = dragRepository.currentEffectDragInformation();
        TimelinePosition position = timelineState.pixelsToSeconds(event.getX());

        EffectMovedCommand command = EffectMovedCommand.builder()
                .withEffectId(draggedEffect.getEffectId())
                .withOriginalClipId(draggedEffect.getClipId())
                .withNewClipId((String) effectNode.getUserData())
                .withLocalNewPosition(position)
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
