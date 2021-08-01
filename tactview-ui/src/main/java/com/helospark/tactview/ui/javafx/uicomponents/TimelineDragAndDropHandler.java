package com.helospark.tactview.ui.javafx.uicomponents;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.google.common.collect.Streams;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.ChangeClipForEffectCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ClipMovedCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ClipResizedCommand;
import com.helospark.tactview.ui.javafx.commands.impl.EffectResizedCommand;
import com.helospark.tactview.ui.javafx.key.CurrentlyPressedKeyRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository.DragDirection;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditMode;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditModeRepository;

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
    private TimelineEditModeRepository timelineEditModeRepository;

    @Slf4j
    private Logger logger;

    public TimelineDragAndDropHandler(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreter, TimelineState timelineState,
            DragRepository dragRepository, SelectedNodeRepository selectedNodeRepository, CurrentlyPressedKeyRepository currentlyPressedKeyRepository,
            UiTimelineManager uiTimelineManager, TimelineEditModeRepository timelineEditModeRepository) {
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
        this.timelineState = timelineState;
        this.dragRepository = dragRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.currentlyPressedKeyRepository = currentlyPressedKeyRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.timelineEditModeRepository = timelineEditModeRepository;
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
        ClipDragInformation currentlyDraggedClip = dragRepository.currentlyDraggedClip();
        if (currentlyDraggedClip != null) {
            String clipId = currentlyDraggedClip.getClipId().get(0);

            if (position.isLessThan(TimelinePosition.ofZero())) {
                position = TimelinePosition.ofZero();
            }

            Set<String> clipIds = new HashSet<>(selectedNodeRepository.getSelectedClipIds());
            Set<TimelineClip> rippleElements = new HashSet<>();

            TimelinePosition leftestPosition = Streams.concat(clipIds.stream(), Stream.of(clipId))
                    .flatMap(clip -> timelineManager.findClipById(clip).stream())
                    .sorted((clip1, clip2) -> clip1.getInterval().getStartPosition().compareTo(clip2.getInterval().getStartPosition()))
                    .findFirst()
                    .map(a -> a.getInterval().getStartPosition())
                    .get();

            if (timelineEditModeRepository.getMode().equals(TimelineEditMode.ALL_CHANNEL_RIPPLE)) {
                rippleElements.addAll(timelineManager.findClipsRightFromPositionAndOnChannelIgnoring(leftestPosition, timelineManager.getAllChannelIndices(), List.of()));
            } else if (timelineEditModeRepository.getMode().equals(TimelineEditMode.SINGLE_CHANNEL_RIPPLE)) {
                List<Integer> channels = Streams.concat(clipIds.stream(), Stream.of(clipId))
                        .flatMap(clip -> timelineManager.findChannelForClipId(clip).stream())
                        .flatMap(channel -> timelineManager.findChannelIndexByChannelId(channel.getId()).stream())
                        .collect(Collectors.toList());
                rippleElements.addAll(timelineManager.findClipsRightFromPositionAndOnChannelIgnoring(leftestPosition, channels, List.of()));
            }

            rippleElements.stream()
                    .forEach(element -> clipIds.add(element.getId()));

            boolean useSpecialPoints = timelineEditModeRepository.isMagnetEditModeEnabled(currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY));

            ClipMovedCommand command = ClipMovedCommand.builder()
                    .withIsRevertable(revertable)
                    .withClipId(clipId)
                    .withAdditionalClipIds(new ArrayList<>(clipIds))
                    .withNewPosition(position)
                    .withPreviousPosition(currentlyDraggedClip.getOriginalPosition())
                    .withOriginalChannelId(currentlyDraggedClip.getOriginalChannelId())
                    .withNewChannelId(channelId)
                    .withTimelineManager(timelineManager)
                    .withEnableJumpingToSpecialPosition(useSpecialPoints)
                    .withMoreMoveExpected(!revertable)
                    .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS).getSeconds()))
                    .withAdditionalPositions(List.of(uiTimelineManager.getCurrentPosition()))
                    .build();

            commandInterpreter.sendWithResult(command).join();
        }
    }

    public void resizeClip(TimelinePosition position, boolean revertable) {
        ClipDragInformation currentlyDraggedEffect = dragRepository.currentlyDraggedClip();
        if (currentlyDraggedEffect != null) {
            List<String> clipId = currentlyDraggedEffect.getClipId();

            boolean useSpecialPoints = timelineEditModeRepository.isMagnetEditModeEnabled(currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY));

            ClipResizedCommand command = ClipResizedCommand.builder()
                    .withClipIds(clipId)
                    .withLeft(dragRepository.getDragDirection().equals(DragRepository.DragDirection.LEFT))
                    .withPosition(position)
                    .withOriginalPosition(currentlyDraggedEffect.getOriginalPosition())
                    .withRevertable(revertable)
                    .withTimelineManager(timelineManager)
                    .withUseSpecialPoints(useSpecialPoints)
                    .withMinimumSize(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MINIMUM_CLIP_SIZE).getSeconds()))
                    .withMoreResizeExpected(!revertable)
                    .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS).getSeconds()))
                    .withTimelineEditMode(timelineEditModeRepository.getMode())
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

        boolean useSpecialPoints = timelineEditModeRepository.isMagnetEditModeEnabled(currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY));

        EffectResizedCommand resizedCommand = EffectResizedCommand.builder()
                .withEffectId(draggedEffect.getEffectId())
                .withLeft(dragDirection.equals(DragDirection.LEFT))
                .withMoreResizeExpected(!revertable)
                .withUseSpecialPoints(useSpecialPoints)
                .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS).getSeconds()))
                .withGlobalPosition(position)
                .withOriginalPosition(draggedEffect.getOriginalPosition())
                .withRevertable(revertable)
                .withTimelineManager(timelineManager)
                .withMinimumLength(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MINIMUM_EFFECT_SIZE).getSeconds()))
                .build();

        commandInterpreter.sendWithResult(resizedCommand);
    }

    public void moveEffect(TimelinePosition position, boolean revertable) {
        EffectDragInformation draggedEffect = dragRepository.currentEffectDragInformation();

        boolean useSpecialPoints = timelineEditModeRepository.isMagnetEditModeEnabled(currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY));

        EffectMovedCommand command = EffectMovedCommand.builder()
                .withEffectId(draggedEffect.getEffectId())
                .withOriginalClipId(draggedEffect.getClipId())
                .withGlobalNewPosition(position)
                .withRevertable(revertable)
                .withOriginalPosition(draggedEffect.getOriginalPosition())
                .withTimelineManager(timelineManager)
                .withEnableJumpingToSpecialPosition(useSpecialPoints)
                .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSecondsWithZoom(20).getSeconds()))
                .withMoreMoveExpected(!revertable)
                .withAdditionalSpecialPositions(List.of(uiTimelineManager.getCurrentPosition()))
                .build();
        commandInterpreter.sendWithResult(command);
    }

    public void moveEffectToDifferentParent(String newClipId, TimelinePosition position) {
        EffectDragInformation dragInformation = dragRepository.currentEffectDragInformation();
        ChangeClipForEffectCommand command = new ChangeClipForEffectCommand(timelineManager, dragInformation.getEffectId(), newClipId, position);
        commandInterpreter.sendWithResult(command);
    }

}
