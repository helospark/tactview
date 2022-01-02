package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.AddExistingClipRequest;
import com.helospark.tactview.core.timeline.ClipChannelPair;
import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;
import com.helospark.tactview.ui.javafx.commands.impl.AddExistingClipsCommand;
import com.helospark.tactview.ui.javafx.key.CurrentlyPressedKeyRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;
import com.helospark.tactview.ui.javafx.uicomponents.EffectDragAdder;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineDragAndDropHandler;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheElement;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheType;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

@Component
public class TimelineCanvasOnDragOverHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimelineCanvasOnDragOverHandler.class);
    private TimelineManagerAccessor timelineAccessor;
    private TimelineDragAndDropHandler timelineDragAndDropHandler;
    private UiCommandInterpreterService commandInterpreter;
    private DragRepository dragRepository;
    private SelectedNodeRepository selectedNodeRepository;
    private EffectDragAdder effectDragAdder;
    private CurrentlyPressedKeyRepository pressedKeyRepository;
    private TimelineState timelineState;
    private LinkClipRepository linkClipRepository;

    private boolean isLoadingInprogress = false; // Move to repository class

    public TimelineCanvasOnDragOverHandler(TimelineManagerAccessor timelineAccessor, TimelineDragAndDropHandler timelineDragAndDropHandler,
            UiCommandInterpreterService commandInterpreter,
            DragRepository dragRepository, SelectedNodeRepository selectedNodeRepository, EffectDragAdder effectDragAdder, CurrentlyPressedKeyRepository pressedKeyRepository,
            TimelineState timelineState,
            LinkClipRepository linkClipRepository) {
        this.timelineAccessor = timelineAccessor;
        this.timelineDragAndDropHandler = timelineDragAndDropHandler;
        this.commandInterpreter = commandInterpreter;
        this.dragRepository = dragRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.effectDragAdder = effectDragAdder;
        this.pressedKeyRepository = pressedKeyRepository;
        this.timelineState = timelineState;
        this.linkClipRepository = linkClipRepository;
    }

    public void onDragOver(TimelineCanvasOnDragOverHandlerRequest request) {
        var event = request.event.get();
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
            if (!isLoadingInprogress && dragRepository.currentlyDraggedClip() == null && ((dbFiles != null && !dbFiles.isEmpty()) || timelineDragAndDropHandler.isStringClip(db))) {
                selectedNodeRepository.clearAllSelectedItems();
                isLoadingInprogress = true;

                try {
                    AddClipsCommand command = timelineDragAndDropHandler.buildAddClipsCommand(channelId, dbFiles, dbString, currentX);

                    AddClipsCommand result = commandInterpreter.synchronousSend(command);
                    List<String> addedClipIds = result.getAddedClipIds();
                    if (addedClipIds.size() > 0) {
                        TimelineInterval originalInterval = timelineAccessor.findClipById(addedClipIds.get(0)).get().getInterval();
                        selectedNodeRepository.clearAndSetSelectedClips(addedClipIds);
                        ClipDragInformation clipDragInformation = new ClipDragInformation(result.getRequestedPosition(), addedClipIds, channelId, 0, originalInterval);
                        dragRepository.onClipDragged(clipDragInformation);
                    }
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
                    Optional<TimelineClip> insertBeforeClip = getInsertBefore(request, channelId);

                    if (insertBeforeClip.isPresent()) {
                        if (dragRepository.getClipDragInformation().getHasMovedWithoutRevert()) {
                            timelineDragAndDropHandler.moveClip(channelId, true, newX); // so revert works properly
                        }
                        timelineDragAndDropHandler.insertClipBefore(insertBeforeClip.get());

                        timelineAccessor.findClipById(dragRepository.getClipDragInformation().getClipIds().get(0))
                                .map(a -> a.getInterval())
                                .ifPresent(interval -> dragRepository.getClipDragInformation().setOriginalInterval(interval));
                    } else {
                        if (dragRepository.currentlyDraggedClip().shouldCopyClip()) {
                            copyCurrentClipOnDrag(dragRepository.currentlyDraggedClip().getClipIds(), channelId, newX);
                        } else {
                            timelineDragAndDropHandler.moveClip(channelId, finished, newX);
                        }
                    }
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

    private void copyCurrentClipOnDrag(List<String> clipIds, String channelId, TimelinePosition newX) {
        TimelineChannel channel = timelineAccessor.findChannelWithId(channelId).get();

        int relativeChannelMove = timelineAccessor.findChannelIndexByChannelId(channelId).get() - timelineAccessor.findChannelIndexForClipId(clipIds.get(0)).get();

        List<TimelineClip> clips = new ArrayList<>();

        List<TimelineClip> allClipsToCopyOriginal = timelineAccessor.resolveClipIdsWithAllLinkedClip(clipIds);

        for (var clip : allClipsToCopyOriginal) {
            int currentChannelIndex = timelineAccessor.findChannelIndexForClipId(clip.getId()).get();
            int newChannelIndex = currentChannelIndex + relativeChannelMove;
            if (newChannelIndex < 0 || newChannelIndex >= timelineAccessor.getChannels().size()) {
                LOGGER.debug("Cannot copy clip, becase {} would be in non-existent channel {}", clip.getId(), newChannelIndex);
                return;
            }
        }

        Map<TimelineClip, TimelineClip> originalToNewClipMap = new HashMap<>();

        List<ClipChannelPair> allClonedClips = allClipsToCopyOriginal.stream()
                .filter(a -> !a.getId().equals(clipIds.get(0)))
                .map(a -> {
                    TimelineClip newClip = a.cloneClip(CloneRequestMetadata.ofDefault());
                    int currentChannelIndex = timelineAccessor.findChannelIndexForClipId(a.getId()).get();
                    int newChannelIndex = currentChannelIndex + relativeChannelMove;
                    TimelineChannel newChannel = timelineAccessor.findChannelOnIndex(newChannelIndex).get();

                    originalToNewClipMap.put(a, newClip);

                    return new ClipChannelPair(newClip, newChannel);
                })
                .collect(Collectors.toList());

        TimelineClip clip = timelineAccessor.findClipById(clipIds.get(0)).get();
        TimelineClip clonedClip = clip.cloneClip(CloneRequestMetadata.ofDefault());

        originalToNewClipMap.put(clip, clonedClip);

        AddExistingClipRequest request = AddExistingClipRequest.builder()
                .withChannel(channel)
                .withClipToAdd(clonedClip)
                .withPosition(Optional.of(newX))
                .withAdditionalClipsToAdd(allClonedClips)
                .build();

        AddExistingClipsCommand addClipCommand = new AddExistingClipsCommand(request, timelineAccessor);
        clips.add(clonedClip);
        commandInterpreter.synchronousSend(addClipCommand);

        setNewClipDragInformationAfterCopy(channelId, addClipCommand, clips, originalToNewClipMap);
    }

    private void setNewClipDragInformationAfterCopy(String channelId, AddExistingClipsCommand commands, List<TimelineClip> clips,
            Map<TimelineClip, TimelineClip> originalToNewClipMap) {
        TimelineClip clip = clips.get(0);
        if (commands.isSuccess()) {
            dragRepository.currentlyDraggedClip().setShouldCopyClip(false);
            List<String> elementIds = clips.stream().map(a -> a.getId()).collect(Collectors.toList());

            ClipDragInformation clipDragInformation = new ClipDragInformation(clip.getGlobalInterval().getStartPosition(),
                    elementIds,
                    channelId,
                    dragRepository.getClipDragInformation().getAnchorPointX(),
                    clip.getGlobalInterval());

            relinkClips(originalToNewClipMap);

            dragRepository.onClipDragged(clipDragInformation);
        }
    }

    private void relinkClips(Map<TimelineClip, TimelineClip> originalToNewClipMap) {
        for (var entry : originalToNewClipMap.entrySet()) {
            List<String> links = linkClipRepository.getLinkedClips(entry.getKey().getId());
            TimelineClip originalElement = entry.getValue();
            for (var link : links) {
                TimelineClip addedElement = findElementInMapById(originalToNewClipMap, link).getValue();
                linkClipRepository.linkClip(originalElement.getId(), addedElement.getId());
            }
        }
    }

    private Entry<TimelineClip, TimelineClip> findElementInMapById(Map<TimelineClip, TimelineClip> originalToNewClipMap, String link) {
        for (var entry : originalToNewClipMap.entrySet()) {
            if (entry.getKey().getId().equals(link)) {
                return entry;
            }
        }
        throw new IllegalStateException("Unable to find element");
    }

    private Optional<TimelineClip> getInsertBefore(TimelineCanvasOnDragOverHandlerRequest request, String channelId) {
        if (!request.selectedElement.isPresent()) {
            return Optional.empty();
        }
        if (!request.selectedElement.get().elementType.equals(TimelineUiCacheType.CLIP)) {
            return Optional.empty();
        }
        if (!request.channel.isPresent()) {
            return Optional.empty();
        }
        String currentlyDraggedClipId = dragRepository.currentlyDraggedClip().getClipIds().get(0);
        Optional<TimelineChannel> optionalChannelForClip = timelineAccessor.findChannelForClipId(currentlyDraggedClipId);
        if (!optionalChannelForClip.isPresent()) {
            return Optional.empty();
        }
        TimelineChannel timelineChannel = optionalChannelForClip.get();
        if (!(request.channel.isPresent() && timelineChannel.getId().equals(channelId))) {
            return Optional.empty();
        }

        for (var clip : request.channel.get().getAllClips()) {
            if (selectedNodeRepository.getSelectedClipIds().contains(clip.getId())) {
                continue;
            }
            if (dragRepository.currentlyDraggedClip().getClipIds().contains(clip.getId())) {
                continue;
            }

            if (Math.abs(timelineState.secondsToPixelsWidthZoomAndTranslate(clip.getInterval().getStartPosition()) - request.getEventX()) < 10) {
                return Optional.ofNullable(clip);
            }
        }

        return Optional.empty();
    }

    private void acceptTransferMode(TimelineCanvasOnDragOverHandlerRequest request) {
        request.event.ifPresent(event -> event.acceptTransferModes(TransferMode.COPY_OR_MOVE));
    }

    private void selectElementOnMouseDrag() {
        if (dragRepository.currentlyDraggedClip() != null) {
            String elementId = dragRepository.currentlyDraggedClip().getClipIds().get(0);
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
        Optional<DragEvent> event;
        Optional<MouseEvent> mouseEvent;
        Optional<TimelineUiCacheElement> selectedElement;
        TimelinePosition xPosition;
        Optional<TimelineChannel> channel;

        private TimelineCanvasOnDragOverHandlerRequest(Builder builder) {
            this.event = builder.event;
            this.selectedElement = builder.selectedElement;
            this.xPosition = builder.xPosition;
            this.channel = builder.channel;
            this.mouseEvent = builder.mouseEvent;
        }

        public double getEventX() {
            if (event.isPresent()) {
                return event.get().getX();
            } else {
                return mouseEvent.get().getX();
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private Optional<DragEvent> event = Optional.empty();
            private Optional<MouseEvent> mouseEvent = Optional.empty();
            private Optional<TimelineUiCacheElement> selectedElement = Optional.empty();
            private TimelinePosition xPosition;
            private Optional<TimelineChannel> channel = Optional.empty();

            private Builder() {
            }

            public Builder withEvent(DragEvent event) {
                this.event = Optional.ofNullable(event);
                return this;
            }

            public Builder withMouseEvent(MouseEvent event) {
                this.mouseEvent = Optional.ofNullable(event);
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
