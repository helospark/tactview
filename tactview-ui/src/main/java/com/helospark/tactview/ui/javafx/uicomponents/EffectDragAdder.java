package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddEffectCommand;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

@Component
public class EffectDragAdder {
    private Rectangle draggedEffect;

    private TimelineManager timelineManager;
    private TimelineState timelineState;
    private UiCommandInterpreterService commandInterpreter;

    public EffectDragAdder(TimelineManager timelineManager, TimelineState timelineState, UiCommandInterpreterService commandInterpreter) {
        this.timelineManager = timelineManager;
        this.timelineState = timelineState;
        this.commandInterpreter = commandInterpreter;
    }

    public void addEffectDragOnClip(Node droppedElement, Group timelineRow) {
        droppedElement.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString() && draggedEffect == null) {
                System.out.println("Adding dragged effect");
                draggedEffect = new Rectangle(100, 40);
                draggedEffect.setTranslateY(40);
                draggedEffect.setFill(Color.rgb(255, 0, 0));
                timelineRow.getChildren().add(draggedEffect);
            }
        });

        droppedElement.setOnDragExited(event -> {
            if (draggedEffect != null) {
                timelineRow.getChildren().remove(draggedEffect);
                draggedEffect = null;
            }
        });

        droppedElement.setOnDragOver(event -> {
            if (draggedEffect != null) {
                draggedEffect.setTranslateX(event.getX() - timelineRow.getLayoutX());

                if (event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.LINK);
                    event.consume();
                }
            }
        });

        droppedElement.setOnDragDropped(event -> {
            Rectangle droppedEffect = draggedEffect;

            draggedEffect = null;
            if (event.getDragboard().hasString()) {
                String effectId = event.getDragboard().getString();

                String clipId = (String) droppedElement.getUserData();

                if (clipId != null) {
                    // TODO! needed?
                    TimelineClip foundClip = timelineManager.findClipById(clipId).orElseThrow(() -> new IllegalStateException("No such clip"));
                    // map x to position
                    TimelinePosition position = timelineState.pixelsToSeconds(event.getX())
                            .from(foundClip.getInterval().getStartPosition());
                    TimelinePosition length = timelineState.pixelsToSeconds(droppedEffect.getWidth()).from(position);

                    AddEffectCommand addEffectCommand = new AddEffectCommand(foundClip.getId(), effectId, new TimelineInterval(position, length), timelineManager);

                    commandInterpreter.sendWithResult(addEffectCommand);
                    //                        idToNode.put(addedEffectId, () -> timelineRow.getChildren().remove(droppedEffect));

                    //                        droppedEffect.setUserData(addedEffectId);
                } else {
                    System.out.println("Null clip");
                }
            }
        });

    }

}
