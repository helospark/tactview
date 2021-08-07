package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;
import com.helospark.tactview.ui.javafx.key.CurrentlyPressedKeyRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;
import com.helospark.tactview.ui.javafx.uicomponents.EffectDragAdder;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineDragAndDropHandler;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheElement;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheType;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;

@Component
public class TimelineCanvasOnDragOverHandler {
    private TimelineManagerAccessor timelineAccessor;
    private TimelineDragAndDropHandler timelineDragAndDropHandler;
    private UiCommandInterpreterService commandInterpreter;
    private DragRepository dragRepository;
    private SelectedNodeRepository selectedNodeRepository;
    private EffectDragAdder effectDragAdder;
    private CurrentlyPressedKeyRepository pressedKeyRepository;

    private boolean isLoadingInprogress = false; // Move to repository class

    public TimelineCanvasOnDragOverHandler(TimelineManagerAccessor timelineAccessor, TimelineDragAndDropHandler timelineDragAndDropHandler, UiCommandInterpreterService commandInterpreter,
            DragRepository dragRepository, SelectedNodeRepository selectedNodeRepository, EffectDragAdder effectDragAdder, CurrentlyPressedKeyRepository pressedKeyRepository) {
        this.timelineAccessor = timelineAccessor;
        this.timelineDragAndDropHandler = timelineDragAndDropHandler;
        this.commandInterpreter = commandInterpreter;
        this.dragRepository = dragRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.effectDragAdder = effectDragAdder;
        this.pressedKeyRepository = pressedKeyRepository;
    }

    public void onDragOver(TimelineCanvasOnDragOverHandlerRequest request) {
        var event = request.event;
        Dragboard db = event.getDragboard();

        boolean hasFile = db.getFiles() != null && !db.getFiles().isEmpty();
        if (db.hasString() || hasFile) {
            if (hasFile || db.getString().startsWith("clip:")) {
                onClipDraggedToCanvas(event, db, request);
            } else if (db.getString().startsWith("effect:")) {
                onEffectDraggedToCanvas(event, db, request);
            }
        } else {
            onDrag(event.getX(), event.getY(), false, request);
        }
    }

    private void onClipDraggedToCanvas(DragEvent event, Dragboard db, TimelineCanvasOnDragOverHandlerRequest request) {
        Optional<TimelineChannel> optionalChannel = request.channel;
        if (optionalChannel.isPresent()) {
            List<File> dbFiles = db.getFiles();
            String dbString = db.getString();
            double currentX = event.getX();
            String channelId = optionalChannel.get().getId();
            AddClipRequest addClipRequest = timelineDragAndDropHandler.addClipRequest(channelId, dbFiles, dbString, currentX);
            if (!isLoadingInprogress && dragRepository.currentlyDraggedClip() == null && ((dbFiles != null && !dbFiles.isEmpty()) || timelineDragAndDropHandler.isStringClip(db))) {
                selectedNodeRepository.clearAllSelectedItems();
                isLoadingInprogress = true;

                try {
                    AddClipsCommand result = commandInterpreter.synchronousSend(new AddClipsCommand(addClipRequest, timelineAccessor));
                    String addedClipId = result.getAddedClipId();
                    TimelineInterval originalInterval = timelineAccessor.findClipById(addedClipId).get().getInterval();
                    ClipDragInformation clipDragInformation = new ClipDragInformation(result.getRequestedPosition(), List.of(addedClipId), channelId, 0, originalInterval);
                    dragRepository.onClipDragged(clipDragInformation);
                    db.clear();
                } catch (Exception e1) {
                    e1.printStackTrace();
                } finally {
                    isLoadingInprogress = false;
                }
            }
        }
    }

    private void onEffectDraggedToCanvas(DragEvent event, Dragboard db, TimelineCanvasOnDragOverHandlerRequest request) {
        Optional<TimelineUiCacheElement> element = request.selectedElement;
        if (element.isPresent() && element.get().elementType.equals(TimelineUiCacheType.CLIP)) {
            TimelinePosition position = request.xPosition;
            boolean result = effectDragAdder.addEffectDragOnClip(element.get().elementId, position, db);
            if (result) {
                db.clear();
            }
        }
    }

    public boolean onDrag(double x, double y, boolean finished, TimelineCanvasOnDragOverHandlerRequest request) {
        TimelinePosition position = request.xPosition;
        if ((dragRepository.currentEffectDragInformation() != null || dragRepository.currentlyDraggedClip() != null)) {
            acceptTransferMode(request);

            if (!pressedKeyRepository.isKeyDown(KeyCode.CONTROL)) {
                selectElementOnMouseDrag();
            }

            if (dragRepository.currentlyDraggedClip() != null) {
                if (!dragRepository.isResizing()) {
                    TimelinePosition newX = TimelinePosition.ofSeconds(position.getSeconds().doubleValue() - dragRepository.currentlyDraggedClip().getAnchorPointX());
                    String channelId = request.channel
                            .map(a -> a.getId())
                            .orElse(timelineAccessor.getChannels().get(0).getId());
                    timelineDragAndDropHandler.moveClip(channelId, finished, newX);
                } else {
                    TimelinePosition newX = position;
                    TimelinePosition relativeMove = position.subtract(dragRepository.currentlyDraggedClip().getLastPosition());
                    timelineDragAndDropHandler.resizeClip(newX, finished, relativeMove);
                }
                dragRepository.currentlyDraggedClip().setLastPosition(position);
                return true;
            } else if (dragRepository.currentEffectDragInformation() != null) {
                acceptTransferMode(request);
                if (dragRepository.isResizing()) {
                    TimelinePosition newX = position;
                    timelineDragAndDropHandler.resizeEffect(newX, finished);
                } else {
                    Optional<TimelineUiCacheElement> optionalElementUnderCursor = request.selectedElement;
                    TimelinePosition newX = TimelinePosition.ofSeconds(position.getSeconds().doubleValue() - dragRepository.currentEffectDragInformation().getAnchorPointX());

                    if (optionalElementUnderCursor.isPresent() && optionalElementUnderCursor.get().elementType == TimelineUiCacheType.CLIP
                            && isClipIdDifferentThanClipUnderCursorAndSupported(optionalElementUnderCursor.get())) {
                        timelineDragAndDropHandler.moveEffectToDifferentParent(optionalElementUnderCursor.get().elementId, newX);
                    } else {
                        timelineDragAndDropHandler.moveEffect(newX, finished);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void acceptTransferMode(TimelineCanvasOnDragOverHandlerRequest request) {
        if (request.event != null) {
            request.event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
    }

    private void selectElementOnMouseDrag() {
        if (dragRepository.currentlyDraggedClip() != null) {
            String elementId = dragRepository.currentlyDraggedClip().getClipId().get(0);
            boolean nodeSelected = selectedNodeRepository.getSelectedClipIds().contains(elementId);
            if (!nodeSelected) {
                selectedNodeRepository.setOnlySelectedClip(elementId);
            }
        } else if (dragRepository.currentEffectDragInformation() != null) {
            String elementId = dragRepository.currentEffectDragInformation().getEffectId();
            boolean nodeSelected = selectedNodeRepository.getSelectedEffectIds().contains(elementId);
            if (!nodeSelected) {
                selectedNodeRepository.setOnlySelectedEffect(elementId);
            }
        }
    }

    private boolean isClipIdDifferentThanClipUnderCursorAndSupported(TimelineUiCacheElement element) {
        String newClipId = element.elementId;
        String effectId = dragRepository.currentEffectDragInformation().getEffectId();
        Optional<StatelessEffect> effect = timelineAccessor.findEffectById(effectId);
        Optional<TimelineClip> originalClip = timelineAccessor.findClipForEffect(effectId);
        Optional<TimelineClip> newClip = timelineAccessor.findClipById(newClipId);

        Optional<String> clipId = originalClip.map(clipA -> clipA.getId());
        boolean isNewClipUnderCursor = clipId.isPresent() && !clipId.get().equals(newClipId);

        boolean doesClipSupportEffect = effect.isPresent() && newClip.isPresent() && newClip.get().effectSupported(effect.get());

        return isNewClipUnderCursor && doesClipSupportEffect;
    }

    public static class TimelineCanvasOnDragOverHandlerRequest {
        DragEvent event;
        Optional<TimelineUiCacheElement> selectedElement;
        TimelinePosition xPosition;
        Optional<TimelineChannel> channel;

        private TimelineCanvasOnDragOverHandlerRequest(Builder builder) {
            this.event = builder.event;
            this.selectedElement = builder.selectedElement;
            this.xPosition = builder.xPosition;
            this.channel = builder.channel;
        }

        public static Builder builder() {
            return new Builder();
        }
        public static final class Builder {
            private DragEvent event;
            private Optional<TimelineUiCacheElement> selectedElement = Optional.empty();
            private TimelinePosition xPosition;
            private Optional<TimelineChannel> channel = Optional.empty();
            private Builder() {
            }

            public Builder withEvent(DragEvent event) {
                this.event = event;
                return this;
            }

            public Builder withSelectedElement(Optional<TimelineUiCacheElement> selectedElement) {
                this.selectedElement = selectedElement;
                return this;
            }

            public Builder withXPosition(TimelinePosition xPosition) {
                this.xPosition = xPosition;
                return this;
            }

            public Builder withChannel(Optional<TimelineChannel> channel) {
                this.channel = channel;
                return this;
            }

            public TimelineCanvasOnDragOverHandlerRequest build() {
                return new TimelineCanvasOnDragOverHandlerRequest(this);
            }
        }

    }
}
