package com.helospark.tactview.ui.javafx.uicomponents;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ClipMovedCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ClipResizedCommand;
import com.helospark.tactview.ui.javafx.commands.impl.EffectResizedCommand;
import com.helospark.tactview.ui.javafx.key.CurrentlyPressedKeyRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository.DragDirection;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.SelectionBoxInformation;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

@Component
public class TimelineDragAndDropHandler {
    private static final KeyCode SPECIAL_POSITION_DISABLE_KEY = KeyCode.ALT; // TODO: make switchable
    private static final int MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS = 30;
    private TimelineManagerAccessor timelineManager;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineState timelineState;
    private DragRepository dragRepository;
    private SelectedNodeRepository selectedNodeRepository;
    private CurrentlyPressedKeyRepository currentlyPressedKeyRepository;
    private UiTimeline uiTimeline;
    private UiTimelineManager uiTimelineManager;

    @Slf4j
    private Logger logger;

    private boolean isLoadingInprogress = false;

    public TimelineDragAndDropHandler(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreter, TimelineState timelineState,
            DragRepository dragRepository, SelectedNodeRepository selectedNodeRepository, CurrentlyPressedKeyRepository currentlyPressedKeyRepository,
            UiTimeline uiTimeline, UiTimelineManager uiTimelineManager) {
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
        this.timelineState = timelineState;
        this.dragRepository = dragRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.currentlyPressedKeyRepository = currentlyPressedKeyRepository;
        this.uiTimeline = uiTimeline;
        this.uiTimelineManager = uiTimelineManager;
    }

    public void addDragAndDrop(Node timeline, Pane timelineRow, String channelId) {
        timeline.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();

            List<File> dbFiles = db.getFiles();
            String dbString = db.getString();
            double currentX = event.getX();
            AddClipRequest addClipRequest = addClipRequest(channelId, dbFiles, dbString, currentX);
            if (!isLoadingInprogress && dragRepository.currentlyDraggedClip() == null && ((dbFiles != null && !dbFiles.isEmpty()) || isStringClip(db))) {
                selectedNodeRepository.clearAllSelectedItems();
                isLoadingInprogress = true;

                try {
                    AddClipsCommand result = commandInterpreter.synchronousSend(new AddClipsCommand(addClipRequest, timelineManager));
                    String addedClipId = result.getAddedClipId();
                    logger.debug("Clip added " + addedClipId);
                    Pane addedClip = timelineState.findClipById(addedClipId).orElseThrow(() -> new RuntimeException("Not found"));
                    ClipDragInformation clipDragInformation = new ClipDragInformation(addedClip, result.getRequestedPosition(), addedClipId, channelId, 0);
                    dragRepository.onClipDragged(clipDragInformation);
                } catch (Exception e1) {
                    logger.warn("Error while adding clip", e1);
                } finally {
                    isLoadingInprogress = false;
                }
            }
        });

        timeline.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            if (dragRepository.currentlyDraggedClip() == null && dragRepository.currentEffectDragInformation() == null &&
                    !(event.getTarget() instanceof Rectangle)) {
                double x = event.getX();
                double y = event.getY() + timeline.getLayoutY();
                dragRepository.onBoxSelectStarted(new Point(x, y));
                Dragboard db = timeline.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("dragging");
                db.setContent(content);
                event.consume();
            }
        });

        timeline.setOnDragOver(event -> {
            double x = event.getX();
            double y = event.getY() + timeline.getLayoutY();

            if (dragRepository.currentlyDraggedClip() != null) {
                event.acceptTransferModes(TransferMode.MOVE);
                if (dragRepository.isResizing()) {
                    resizeClip(event, false);
                } else {
                    moveClip(event, channelId, false);
                }
            } else if (dragRepository.currentEffectDragInformation() != null) {
                if (dragRepository.isResizing()) {
                    resizeEffect(event, false);
                } else {
                    moveEffect(event, false);
                }
                event.acceptTransferModes(TransferMode.LINK);
            } else if (dragRepository.isBoxSelectInProgress()) {
                SelectionBoxInformation selectionBox = dragRepository.getSelectionBoxInformation();
                uiTimeline.updateSelectionBox(selectionBox.startPoint, new Point(x, y));
                updateSelectedNodes(uiTimeline.getSelectionRectangle());
            }

            ZoomableScrollPane pane = timelineState.getTimeLineScrollPane();

            Bounds paneBounds = pane.getViewportBounds();

            scrollRightWhenNeeded(x, paneBounds);
            scrollLeftWhenNeeded(x, paneBounds);
            scrollDownWhenNeeded(y, paneBounds);
            scrollUpWhenNeeded(y, paneBounds);
        });

        timeline.setOnDragDone(event -> {
            if (dragRepository.isBoxSelectInProgress()) {
                uiTimeline.selectionBoxEnded();
                dragRepository.onBoxSelectEnded();
            }
        });

        timeline.setOnDragDropped(event -> {
            if (dragRepository.currentlyDraggedClip() != null) {
                if (dragRepository.isResizing()) {
                    resizeClip(event, true);
                } else {
                    moveClip(event, channelId, true);
                }
                event.getDragboard().clear();
                event.setDropCompleted(true);
                dragRepository.clearClipDrag();
                event.consume();
            } else if (dragRepository.currentEffectDragInformation() != null) {
                if (dragRepository.isResizing()) {
                    resizeEffect(event, true);
                } else {
                    moveEffect(event, true);
                }
                event.getDragboard().clear();
                event.setDropCompleted(true);
                dragRepository.clearEffectDrag();
                event.consume();
            } else if (dragRepository.isBoxSelectInProgress()) {
                uiTimeline.selectionBoxEnded();
            }
            timelineState.getMoveSpecialPointLineProperties().setEnabledProperty(false);
        });

    }

    private void updateSelectedNodes(Rectangle selectionRectangle) {
        timelineState.getAllClips()
                .filter(a -> selectionRectangle.intersects(getBoundsWithIncludeParent(a)))
                .forEach(a -> selectedNodeRepository.addSelectedClip(a));
        //        timelineState.getAllEffects()
        //                .filter(a -> selectionRectangle.getBoundsInParent().intersects(getBoundsWithIncludeParentParent(a)))
        //                .forEach(a -> selectedNodeRepository.addSelectedEffect(a));
    }

    private Bounds getBoundsWithIncludeParent(Node selectionRectangle) {
        Bounds inParent = selectionRectangle.getBoundsInParent();
        return new BoundingBox(inParent.getMinX(), selectionRectangle.getParent().getParent().getLayoutY() + inParent.getMinY(), inParent.getWidth(), inParent.getHeight());
    }

    private void scrollRightWhenNeeded(double x, Bounds paneBounds) {
        double rightX = -1.0 * paneBounds.getMinX() + paneBounds.getWidth();

        double distance = (rightX - x);

        if (distance < 50) {
            double scrollStrength = (1.0 - (distance / 50.0)) * 0.01;
            timelineState.horizontalScroll(scrollStrength);
        }
    }

    private void scrollLeftWhenNeeded(double x, Bounds paneBounds) {
        double leftX = -1.0 * paneBounds.getMinX();

        double distance = (x - leftX);

        if (distance < 50) {
            double scrollStrength = (1.0 - (distance / 50.0)) * 0.01;
            timelineState.horizontalScroll(-scrollStrength);
        }
    }

    private void scrollUpWhenNeeded(double y, Bounds paneBounds) {
        double topY = -1.0 * paneBounds.getMinY();

        double distance = (y - topY);

        if (distance < 30) {
            double scrollStrength = (1.0 - (distance / 30.0)) * 0.01;
            timelineState.verticalScroll(-scrollStrength);
        }
    }

    private void scrollDownWhenNeeded(double y, Bounds paneBounds) {
        double bottomY = -1.0 * paneBounds.getMinY() + paneBounds.getHeight();

        double distance = (bottomY - y);

        if (distance < 30) {
            double scrollStrength = (1.0 - (distance / 30.0)) * 0.01;
            timelineState.verticalScroll(scrollStrength);
        }
    }

    private AddClipRequest addClipRequest(String channelId, List<File> dbFiles, String dbString, double currentX) {
        String filePath = extractFilePathOrNull(dbFiles);
        String proceduralClipId = extractProceduralEffectOrNull(dbString);
        TimelinePosition position = timelineState.pixelsToSeconds(currentX);

        return AddClipRequest.builder()
                .withChannelId(channelId)
                .withPosition(position)
                .withFilePath(filePath)
                .withProceduralClipId(proceduralClipId)
                .build();
    }

    private void moveClip(DragEvent event, String channelId, boolean revertable) {
        ClipDragInformation currentlyDraggedEffect = dragRepository.currentlyDraggedClip();
        if (currentlyDraggedEffect != null) {
            String clipId = currentlyDraggedEffect.getClipId();

            TimelinePosition position = timelineState.pixelsToSeconds(event.getX() - currentlyDraggedEffect.getAnchorPointX());

            if (position.isGreaterThan(TimelinePosition.ofZero())) {
                ClipMovedCommand command = ClipMovedCommand.builder()
                        .withIsRevertable(revertable)
                        .withClipId(clipId)
                        .withAdditionalClipIds(selectedNodeRepository.getSelectedClipIds())
                        .withNewPosition(position)
                        .withPreviousPosition(currentlyDraggedEffect.getOriginalPosition())
                        .withOriginalChannelId(currentlyDraggedEffect.getOriginalChannelId())
                        .withNewChannelId(channelId)
                        .withTimelineManager(timelineManager)
                        .withEnableJumpingToSpecialPosition(!currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY))
                        .withMoreMoveExpected(!revertable)
                        .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSeconds(MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS).getSeconds()))
                        .withAdditionalPositions(List.of(uiTimelineManager.getCurrentPosition()))
                        .build();

                commandInterpreter.sendWithResult(command).join();
            }
        }
    }

    private void resizeClip(DragEvent event, boolean revertable) {
        ClipDragInformation currentlyDraggedEffect = dragRepository.currentlyDraggedClip();
        if (currentlyDraggedEffect != null) {
            String clipId = currentlyDraggedEffect.getClipId();
            TimelinePosition position = timelineState.pixelsToSeconds(event.getX());

            ClipResizedCommand command = ClipResizedCommand.builder()
                    .withClipId(clipId)
                    .withLeft(dragRepository.getDragDirection().equals(DragRepository.DragDirection.LEFT))
                    .withPosition(position)
                    .withRevertable(revertable)
                    .withTimelineManager(timelineManager)
                    .withUseSpecialPoints(!currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY))
                    .withMoreResizeExpected(!revertable)
                    .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSeconds(MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS).getSeconds()))
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

    private String extractProceduralEffectOrNull(String dbString) {
        String proceduralClipId = dbString;
        if (proceduralClipId != null && proceduralClipId.startsWith("clip:")) {
            proceduralClipId = proceduralClipId.replaceFirst("clip:", "");
        } else {
            proceduralClipId = null;
        }
        return proceduralClipId;
    }

    private String extractFilePathOrNull(List<File> dbFiles) {
        if (dbFiles == null) {
            return null;
        }
        return dbFiles.stream().findFirst().map(f -> f.getAbsolutePath()).orElse(null);
    }

    private void resizeEffect(DragEvent event, boolean revertable) {
        EffectDragInformation draggedEffect = dragRepository.currentEffectDragInformation();

        double x = event.getX();

        EffectResizedCommand resizedCommand = EffectResizedCommand.builder()
                .withEffectId(draggedEffect.getEffectId())
                .withLeft(dragRepository.getDragDirection().equals(DragDirection.LEFT))
                .withMoreResizeExpected(!revertable)
                .withUseSpecialPoints(!currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY))
                .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSeconds(MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS).getSeconds()))
                .withGlobalPosition(timelineState.pixelsToSeconds(x))
                .withRevertable(revertable)
                .withTimelineManager(timelineManager)
                .build();

        commandInterpreter.sendWithResult(resizedCommand);
    }

    private void moveEffect(DragEvent event, boolean revertable) {
        EffectDragInformation draggedEffect = dragRepository.currentEffectDragInformation();
        TimelinePosition position = timelineState.pixelsToSeconds(event.getX() - draggedEffect.getAnchorPointX());

        EffectMovedCommand command = EffectMovedCommand.builder()
                .withEffectId(draggedEffect.getEffectId())
                .withOriginalClipId(draggedEffect.getClipId())
                .withGlobalNewPosition(position)
                .withRevertable(revertable)
                .withOriginalPosition(draggedEffect.getOriginalPosition())
                .withTimelineManager(timelineManager)
                .withEnableJumpingToSpecialPosition(!currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY))
                .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSeconds(20).getSeconds()))
                .withMoreMoveExpected(!revertable)
                .withAdditionalSpecialPositions(List.of(uiTimelineManager.getCurrentPosition()))
                .build();
        commandInterpreter.sendWithResult(command);
    }

}
