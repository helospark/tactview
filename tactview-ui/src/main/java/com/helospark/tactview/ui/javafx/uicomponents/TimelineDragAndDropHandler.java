package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.concurrent.CompletableFuture;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactoryChain;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ClipMovedCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ClipResizedCommand;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

@Component
public class TimelineDragAndDropHandler {
    private ClipFactoryChain clipFactoryChain;
    private TimelineManager timelineManager;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineState timelineState;
    private DragRepository dragRepository;

    public TimelineDragAndDropHandler(ClipFactoryChain clipFactoryChain, TimelineManager timelineManager, UiCommandInterpreterService commandInterpreter, TimelineState timelineState,
            DragRepository dragRepository) {
        this.clipFactoryChain = clipFactoryChain;
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
        this.timelineState = timelineState;
        this.dragRepository = dragRepository;
    }

    public void addDragAndDrop(Node timeline, Pane timelineRow, String channelId) {
        timeline.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();

            if (dragRepository.currentlyDraggedClip() == null && (db.hasFiles() || isStringClip(db))) {
                AddClipRequest metadataRequest = createAddClipRequest(db);
                CompletableFuture.supplyAsync(() -> {
                    return clipFactoryChain.readMetadata(metadataRequest);
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                }).thenAccept(metadata -> {
                    Platform.runLater(() -> {
                        System.out.println("K=" + metadata);
                        AddClipRequest addClipRequest = addClipRequest(channelId, event);
                        System.out.println(addClipRequest);
                        commandInterpreter.sendWithResult(new AddClipsCommand(addClipRequest, timelineManager))
                                .exceptionally(e -> {
                                    e.printStackTrace();
                                    return null;
                                })
                                .thenAccept(res -> {
                                    try {
                                        String addedClipId = res.getAddedClipId();
                                        System.out.println("Clip added " + addedClipId);
                                        Group addedClip = timelineState.findClipById(addedClipId).orElseThrow(() -> new RuntimeException("Not found"));
                                        ClipDragInformation clipDragInformation = new ClipDragInformation(addedClip, res.getRequestedPosition(), addedClipId, channelId);
                                        dragRepository.onClipDragged(clipDragInformation);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                });
                    });
                });
            }
        });

        timeline.setOnDragExited(event -> {

        });

        timeline.setOnDragOver(event -> {
            if (dragRepository.currentlyDraggedClip() != null) {
                event.acceptTransferModes(TransferMode.MOVE);
                if (dragRepository.isResizing()) {
                    resizeClip(event, false);
                } else {
                    moveClip(event, channelId, false);
                }
            }
        });

        timeline.setOnDragDropped(event -> {
            if (dragRepository.currentlyDraggedClip() != null) {
                if (dragRepository.isResizing()) {
                    resizeClip(event, true);
                } else {
                    moveClip(event, channelId, true);
                }
                dragRepository.clearClipDrag();
            }
        });

    }

    private AddClipRequest addClipRequest(String channelId, DragEvent event) {
        String filePath = extractFilePathOrNull(event.getDragboard());
        String proceduralClipId = extractProceduralEffectOrNull(event.getDragboard());
        TimelinePosition position = timelineState.pixelsToSeconds(event.getX());

        return AddClipRequest.builder()
                .withChannelId(channelId)
                .withPosition(position)
                .withFilePath(filePath)
                .withProceduralClipId(proceduralClipId)
                .build();
    }

    private AddClipRequest createAddClipRequest(Dragboard db) {
        String file = extractFilePathOrNull(db);
        String proceduralClipId = extractProceduralEffectOrNull(db);
        String finalProceduralClipId = proceduralClipId;
        return AddClipRequest.builder()
                .withFilePath(file) // todo: what about rest of params?
                .withProceduralClipId(finalProceduralClipId)
                .build();
    }

    private void moveClip(DragEvent event, String channelId, boolean revertable) {
        ClipDragInformation currentlyDraggedEffect = dragRepository.currentlyDraggedClip();
        if (currentlyDraggedEffect != null) {
            String clipId = currentlyDraggedEffect.getClipId();
            TimelinePosition position = timelineState.pixelsToSeconds(event.getX());

            ClipMovedCommand command = ClipMovedCommand.builder()
                    .withIsRevertable(revertable)
                    .withClipId(clipId)
                    .withNewPosition(position)
                    .withPreviousPosition(currentlyDraggedEffect.getOriginalPosition())
                    .withOriginalChannelId(currentlyDraggedEffect.getOriginalChannelId())
                    .withNewChannelId(channelId)
                    .withTimelineManager(timelineManager)
                    .build();

            commandInterpreter.sendWithResult(command);
        }
    }

    private void resizeClip(DragEvent event, boolean b) {
        ClipDragInformation currentlyDraggedEffect = dragRepository.currentlyDraggedClip();
        if (currentlyDraggedEffect != null) {
            String clipId = currentlyDraggedEffect.getClipId();
            TimelinePosition position = timelineState.pixelsToSeconds(event.getX());

            ClipResizedCommand command = ClipResizedCommand.builder()
                    .withClipId(clipId)
                    .withLeft(dragRepository.getDragDirection().equals(DragRepository.DragDirection.LEFT))
                    .withPosition(position)
                    .withRevertable(b)
                    .withTimelineManager(timelineManager)
                    .build();
            commandInterpreter.sendWithResult(command);
        }
    }

    private boolean isStringClip(Dragboard db) {
        String id = db.getString();
        if (id != null) {
            return id.startsWith("clip:");
        }
        return false;
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

}
