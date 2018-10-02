package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.concurrent.CompletableFuture;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactoryChain;
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
    private ClipFactoryChain clipFactoryChain;
    private TimelineManager timelineManager;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineState timelineState;

    private Rectangle draggedItem = null;

    public TimelineDragAndDropHandler(ClipFactoryChain clipFactoryChain, TimelineManager timelineManager, UiCommandInterpreterService commandInterpreter, TimelineState timelineState) {
        this.clipFactoryChain = clipFactoryChain;
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
        this.timelineState = timelineState;
    }

    public void addDragAndDrop(Node timeline, Pane timelineRow, String channelId) {
        timeline.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            System.out.println("a " + db.getFiles().size());
            draggedItem = new Rectangle(300, 50);
            timelineRow.getChildren().add(draggedItem);
            String file = extractFilePathOrNull(db);
            String proceduralClipId = extractProceduralEffectOrNull(db);
            String finalProceduralClipId = proceduralClipId;
            CompletableFuture.supplyAsync(() -> {
                AddClipRequest request = AddClipRequest.builder()
                        .withFilePath(file) // todo: what about rest of params?
                        .withProceduralClipId(finalProceduralClipId)
                        .build();
                return clipFactoryChain.readMetadata(request);
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
                //                if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.LINK);
                event.consume();
                //                }
            }
        });

        timeline.setOnDragDropped(event -> {
            removeDraggedItem(timelineRow);

            draggedItem = null;
            String filePath = extractFilePathOrNull(event.getDragboard());
            String proceduralClipId = extractProceduralEffectOrNull(event.getDragboard());
            TimelinePosition position = timelineState.pixelsToSeconds(event.getX());

            AddClipRequest request = AddClipRequest.builder()
                    .withChannelId(channelId)
                    .withPosition(position)
                    .withFilePath(filePath)
                    .withProceduralClipId(proceduralClipId)
                    .build();

            commandInterpreter.sendWithResult(new AddClipsCommand(request, timelineManager));
        });

    }

    private String extractProceduralEffectOrNull(Dragboard db) {
        String proceduralClipId = db.getString();
        if (proceduralClipId.startsWith("clip:")) {
            proceduralClipId = proceduralClipId.replaceFirst("clip:", "");
        } else {
            proceduralClipId = null;
        }
        return proceduralClipId;
    }

    private String extractFilePathOrNull(Dragboard db) {
        return db.getFiles().stream().findFirst().map(f -> f.getAbsolutePath()).orElse(null);
    }

    private void removeDraggedItem(Pane timelineRow) {
        if (draggedItem != null) {
            timelineRow.getChildren().remove(draggedItem);
            draggedItem = null;
        }
    }

}
