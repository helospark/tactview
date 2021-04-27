package com.helospark.tactview.ui.javafx.uicomponents;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.ClipMovedCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ClipResizedCommand;
import com.helospark.tactview.ui.javafx.commands.impl.EffectResizedCommand;
import com.helospark.tactview.ui.javafx.key.CurrentlyPressedKeyRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository.DragDirection;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;

import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;

@Component
public class TimelineDragAndDropHandler {
    private static final KeyCode SPECIAL_POSITION_DISABLE_KEY = KeyCode.ALT; // TODO: make switchable
    private static final int MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS = 30;
    private static final int MINIMUM_CLIP_SIZE = 10;
    private static final int MINIMUM_EFFECT_SIZE = 10;
    private TimelineManagerAccessor timelineManager;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineState timelineState;
    private DragRepository dragRepository;
    private SelectedNodeRepository selectedNodeRepository;
    private CurrentlyPressedKeyRepository currentlyPressedKeyRepository;
    private UiTimelineManager uiTimelineManager;

    @Slf4j
    private Logger logger;

    public TimelineDragAndDropHandler(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreter, TimelineState timelineState,
            DragRepository dragRepository, SelectedNodeRepository selectedNodeRepository, CurrentlyPressedKeyRepository currentlyPressedKeyRepository,
            UiTimelineManager uiTimelineManager) {
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
        this.timelineState = timelineState;
        this.dragRepository = dragRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.currentlyPressedKeyRepository = currentlyPressedKeyRepository;
        this.uiTimelineManager = uiTimelineManager;
    }

    public AddClipRequest addClipRequest(String channelId, List<File> dbFiles, String dbString, double currentX) {
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

    public boolean isStringClip(Dragboard db) {
        String id = db.getString();
        if (id != null) {
            return id.startsWith("clip:");
        }
        return false;
    }

    public void moveClip(String channelId, boolean revertable, TimelinePosition position) {
        ClipDragInformation currentlyDraggedEffect = dragRepository.currentlyDraggedClip();
        if (currentlyDraggedEffect != null) {
            String clipId = currentlyDraggedEffect.getClipId();

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
                        .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS).getSeconds()))
                        .withAdditionalPositions(List.of(uiTimelineManager.getCurrentPosition()))
                        .build();

                commandInterpreter.sendWithResult(command).join();
            }
        }
    }

    public void resizeClip(TimelinePosition position, boolean revertable) {
        ClipDragInformation currentlyDraggedEffect = dragRepository.currentlyDraggedClip();
        if (currentlyDraggedEffect != null) {
            String clipId = currentlyDraggedEffect.getClipId();

            ClipResizedCommand command = ClipResizedCommand.builder()
                    .withClipId(clipId)
                    .withLeft(dragRepository.getDragDirection().equals(DragRepository.DragDirection.LEFT))
                    .withPosition(position)
                    .withRevertable(revertable)
                    .withTimelineManager(timelineManager)
                    .withUseSpecialPoints(!currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY))
                    .withMinimumSize(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MINIMUM_CLIP_SIZE).getSeconds()))
                    .withMoreResizeExpected(!revertable)
                    .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS).getSeconds()))
                    .build();
            commandInterpreter.sendWithResult(command);
        }
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

    public void resizeEffect(TimelinePosition position, boolean revertable) {
        EffectDragInformation draggedEffect = dragRepository.currentEffectDragInformation();
        DragDirection dragDirection = dragRepository.getDragDirection();

        if (draggedEffect == null || dragDirection == null) {
            return;
        }

        EffectResizedCommand resizedCommand = EffectResizedCommand.builder()
                .withEffectId(draggedEffect.getEffectId())
                .withLeft(dragDirection.equals(DragDirection.LEFT))
                .withMoreResizeExpected(!revertable)
                .withUseSpecialPoints(!currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY))
                .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS).getSeconds()))
                .withGlobalPosition(position)
                .withRevertable(revertable)
                .withTimelineManager(timelineManager)
                .withMinimumLength(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MINIMUM_EFFECT_SIZE).getSeconds()))
                .build();

        commandInterpreter.sendWithResult(resizedCommand);
    }

    public void moveEffect(TimelinePosition position, boolean revertable) {
        EffectDragInformation draggedEffect = dragRepository.currentEffectDragInformation();

        EffectMovedCommand command = EffectMovedCommand.builder()
                .withEffectId(draggedEffect.getEffectId())
                .withOriginalClipId(draggedEffect.getClipId())
                .withGlobalNewPosition(position)
                .withRevertable(revertable)
                .withOriginalPosition(draggedEffect.getOriginalPosition())
                .withTimelineManager(timelineManager)
                .withEnableJumpingToSpecialPosition(!currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY))
                .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSecondsWithZoom(20).getSeconds()))
                .withMoreMoveExpected(!revertable)
                .withAdditionalSpecialPositions(List.of(uiTimelineManager.getCurrentPosition()))
                .build();
        commandInterpreter.sendWithResult(command);
    }

}
