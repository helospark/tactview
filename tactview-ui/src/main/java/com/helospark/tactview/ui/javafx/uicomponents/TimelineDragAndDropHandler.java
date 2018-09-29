package com.helospark.tactview.ui.javafx.uicomponents;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaDecoder;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;

import javafx.scene.Node;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

@Component
public class TimelineDragAndDropHandler {
    private MediaDecoder mediaDecoder;
    private TimelineManager timelineManager;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineState timelineState;

    private Rectangle draggedItem = null;

    public TimelineDragAndDropHandler(MediaDecoder mediaDecoder, TimelineManager timelineManager, UiCommandInterpreterService commandInterpreter, TimelineState timelineState) {
        this.mediaDecoder = mediaDecoder;
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
        this.timelineState = timelineState;
    }

    public void addDragAndDrop(Node timeline, Pane timelineRow, String channelId) {
        timeline.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            System.out.println("a " + db.getFiles().size());
            if (!db.getFiles().isEmpty()) {
                draggedItem = new Rectangle(300, 50);
                timelineRow.getChildren().add(draggedItem);
                File file = db.getFiles().get(0);
                CompletableFuture.supplyAsync(() -> {
                    return mediaDecoder.readMetadata(file);
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                }).thenAccept(b -> {
                    int width = timelineState.secondsToPixels(b.getLength());
                    System.out.println("Setting width to " + width);
                    if (draggedItem != null) {
                        draggedItem.setWidth(width);
                    }
                });
            }
        });

        timeline.setOnDragExited(event -> {
            removeDraggedItem(timelineRow);
        });

        timeline.setOnDragOver(event -> {
            if (draggedItem != null) {
                draggedItem.setTranslateX(event.getX() - timelineRow.getLayoutX());

                //            if (event.getY() > 180) {
                //                timeLineScrollPane.setVvalue(timeLineScrollPane.getVvalue() + 0.01);
                //            }
                if (event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.LINK);
                    event.consume();
                }
            }
        });

        timeline.setOnDragDropped(event -> {
            removeDraggedItem(timelineRow);

            draggedItem = null;
            if (event.getDragboard().hasFiles()) {
                File file = event.getDragboard().getFiles().get(0);
                TimelinePosition position = timelineState.pixelsToSeconds(event.getX());

                commandInterpreter.sendWithResult(new AddClipsCommand(channelId, position, file.getAbsolutePath(), timelineManager));
            }
        });

    }

    private void removeDraggedItem(Pane timelineRow) {
        if (draggedItem != null) {
            timelineRow.getChildren().remove(draggedItem);
            draggedItem = null;
        }
    }

}
