package com.helospark.tactview.ui.javafx.uicomponents;

import java.io.File;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaDecoder;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.TimelineImagePatternService;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

@Component
public class TimelineDragAndDropHandler {
    public static final BigDecimal PIXEL_PER_SECOND = new BigDecimal(10L);
    private MediaDecoder mediaDecoder;
    private TimelineManager timelineManager;
    private Rectangle draggedItem = null;
    private TimelineImagePatternService timelineImagePatternService;

    public TimelineDragAndDropHandler(MediaDecoder mediaDecoder, TimelineManager timelineManager, TimelineImagePatternService timelineImagePatternService) {
        this.mediaDecoder = mediaDecoder;
        this.timelineManager = timelineManager;
        this.timelineImagePatternService = timelineImagePatternService;
    }

    public void addDragAndDrop(Node timeline, Pane timelineRow, int index, double scale) {

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
                    int width = b.getLength().getSeconds().multiply(PIXEL_PER_SECOND).intValue();
                    System.out.println("Setting width to " + width);
                    if (draggedItem != null) {
                        draggedItem.setWidth(width);
                    }
                });
            }
        });

        timeline.setOnDragExited(event -> {
            if (draggedItem != null) {
                timelineRow.getChildren().remove(draggedItem);
                draggedItem = null;
            }
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
            Rectangle droppedElement = draggedItem;

            draggedItem = null;
            //            System.out.println("CHANNEL " + index + " dragdone " + event.getX() + " " + event.getY() + " " + timeLineScrollPane.getVvalue());
            if (event.getDragboard().hasFiles()) {
                File file = event.getDragboard().getFiles().get(0);
                MediaMetadata metadata = mediaDecoder.readMetadata(file);
                int timelineWidth = (int) (droppedElement.getWidth() * scale);
                // map x to position
                BigDecimal position = new BigDecimal(event.getX())
                        .multiply(BigDecimal.ONE) // zoom dummy
                        .subtract(BigDecimal.ZERO) // scroll dummy
                        .divide(PIXEL_PER_SECOND);

                // todo: command pattern here
                TimelineClip resource = timelineManager.addResource(index, new TimelinePosition(position), file.getAbsolutePath());

                droppedElement.setUserData(resource);
                addEffectDragOnClip(droppedElement, timelineRow);

                timelineImagePatternService.createTimelinePattern(file, metadata, timelineWidth)
                        .exceptionally(e -> {
                            e.printStackTrace();
                            return null;
                        })
                        .thenAccept(fillImage -> {
                            System.out.println("Setting image");
                            if (droppedElement != null) {
                                Platform.runLater(() -> droppedElement.setFill(new ImagePattern(fillImage)));
                            }
                        });

            }
        });

    }

    Rectangle draggedEffect = null;

    private void addEffectDragOnClip(Rectangle droppedElement, Pane timelineRow) {
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

                TimelineClip clip = (TimelineClip) droppedElement.getUserData();

                if (clip != null) {
                    // map x to position
                    BigDecimal position = new BigDecimal(event.getX())
                            .multiply(BigDecimal.ONE) // zoom dummy
                            .subtract(BigDecimal.ZERO) // scroll dummy
                            .divide(PIXEL_PER_SECOND)
                            .subtract(clip.getInterval().getStartPosition().getSeconds());
                    double length = droppedEffect.getWidth() / PIXEL_PER_SECOND.doubleValue();
                    // todo: command pattern here
                    StatelessEffect effect = timelineManager.addEffectForClip(clip.getId(), effectId, new TimelineInterval(new TimelinePosition(position), new TimelineLength(BigDecimal.valueOf(length))));
                    droppedEffect.setUserData(effect);
                } else {
                    System.out.println("Null clip");
                }
            }
        });

    }

}
