package com.helospark.tactview.ui.javafx.uicomponents;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.google.common.collect.Streams;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ChangeClipForEffectCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ClipInsertCommand;
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
    private MessagingService messagingService;

    @Slf4j
    private Logger logger;

    public TimelineDragAndDropHandler(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreter, TimelineState timelineState,
            DragRepository dragRepository, SelectedNodeRepository selectedNodeRepository, CurrentlyPressedKeyRepository currentlyPressedKeyRepository,
            UiTimelineManager uiTimelineManager, TimelineEditModeRepository timelineEditModeRepository, MessagingService messagingService) {
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
        this.timelineState = timelineState;
        this.dragRepository = dragRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.currentlyPressedKeyRepository = currentlyPressedKeyRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.timelineEditModeRepository = timelineEditModeRepository;
        this.messagingService = messagingService;
    }

    public AddClipsCommand buildAddClipsCommand(String channelId, List<File> dbFiles, String dbString, double currentX) {
        List<String> filePaths = extractFilePaths(dbFiles);
        String proceduralClipId = extractProceduralEffectOrNull(dbString);
        TimelinePosition position = timelineState.pixelsToSeconds(currentX);

        return AddClipsCommand.builder()
                .withChannelId(channelId)
                .withPosition(position)
                .withFilePaths(filePaths)
                .withProceduralClipId(proceduralClipId)
                .withTimelineManager(timelineManager)
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
        ClipMovedCommand command = createMoveClipCommand(channelId, revertable, position);
        if (command != null) {
            ClipMovedCommand result = commandInterpreter.sendWithResult(command).join();
            dragRepository.currentlyDraggedClip().setHasMovedWithoutRevert(!revertable && (dragRepository.currentlyDraggedClip().getHasMovedWithoutRevert() || result.hasMoved()));
        }
    }

    public ClipMovedCommand createMoveClipCommand(String channelId, boolean revertable, TimelinePosition position) {
        ClipDragInformation currentlyDraggedClip = dragRepository.currentlyDraggedClip();
        if (currentlyDraggedClip != null) {
            String clipId = currentlyDraggedClip.getClipId().get(0);

            if (position.isLessThan(TimelinePosition.ofZero())) {
                position = TimelinePosition.ofZero();
            }

            Set<String> clipIds = new HashSet<>(selectedNodeRepository.getSelectedClipIds());
            Set<TimelineClip> rippleElements = new HashSet<>();

            Optional<TimelinePosition> leftestPositionOptional = Streams.concat(clipIds.stream(), Stream.of(clipId))
                    .flatMap(clip -> timelineManager.findClipById(clip).stream())
                    .sorted((clip1, clip2) -> clip1.getInterval().getStartPosition().compareTo(clip2.getInterval().getStartPosition()))
                    .findFirst()
                    .map(a -> a.getInterval().getStartPosition());
            if (leftestPositionOptional.isEmpty()) {
                return null;
            }
            var leftestPosition = leftestPositionOptional.get();

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

            return ClipMovedCommand.builder()
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
        } else {
            return null;
        }
    }

    public void resizeClip(TimelinePosition position, boolean revertable, TimelinePosition relativeMove) {
        ClipDragInformation currentlyDraggedEffect = dragRepository.currentlyDraggedClip();
        if (currentlyDraggedEffect != null) {
            List<String> clipId = currentlyDraggedEffect.getClipId();

            boolean useSpecialPoints = timelineEditModeRepository.isMagnetEditModeEnabled(currentlyPressedKeyRepository.isKeyDown(SPECIAL_POSITION_DISABLE_KEY));

            ClipResizedCommand command = ClipResizedCommand.builder()
                    .withClipIds(clipId)
                    .withLeft(dragRepository.getDragDirection().equals(DragRepository.DragDirection.LEFT))
                    .withPosition(position)
                    .withOriginalPosition(currentlyDraggedEffect.getOriginalPosition())
                    .withOriginalInterval(currentlyDraggedEffect.getOriginalInterval())
                    .withRevertable(revertable)
                    .withTimelineManager(timelineManager)
                    .withUseSpecialPoints(useSpecialPoints)
                    .withMinimumSize(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MINIMUM_CLIP_SIZE).getSeconds()))
                    .withMoreResizeExpected(!revertable)
                    .withMaximumJumpLength(new TimelineLength(timelineState.pixelsToSecondsWithZoom(MAXIMUM_SPECIAL_POINT_JUMP_LENGTH_IN_PIXELS).getSeconds()))
                    .withTimelineEditMode(timelineEditModeRepository.getMode())
                    .withRelativeMove(relativeMove)
                    .build();
            commandInterpreter.sendWithResult(command).join();
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

    private List<String> extractFilePaths(List<File> dbFiles) {
        if (dbFiles == null) {
            return null;
        }
        return dbFiles.stream()
                .map(f -> f.getAbsolutePath())
                .collect(Collectors.toList());
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

        commandInterpreter.sendWithResult(resizedCommand).join();
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
        commandInterpreter.sendWithResult(command).join();
    }

    public void moveEffectToDifferentParent(String newClipId, TimelinePosition position) {
        EffectDragInformation dragInformation = dragRepository.currentEffectDragInformation();
        ChangeClipForEffectCommand command = new ChangeClipForEffectCommand(timelineManager, dragInformation.getEffectId(), newClipId, position);
        commandInterpreter.sendWithResult(command).join();
    }

    public void insertClipBefore(TimelineClip timelineClip) {
        ClipInsertCommand clipInsertCommand = createInsertClipBeforeCommand(timelineClip);
        commandInterpreter.sendWithResult(clipInsertCommand).join();
    }

    public ClipInsertCommand createInsertClipBeforeCommand(TimelineClip timelineClip) {
        ClipInsertCommand clipInsertCommand = ClipInsertCommand.builder()
                .withClipIdsToInsert(selectedNodeRepository.getSelectedClipIds())
                .withInsertInPlace(timelineClip)
                .withMessagingService(messagingService)
                .withTimelineEditMode(TimelineEditMode.NORMAL) // TODO: fill later
                .withTimelineManager(timelineManager)
                .build();
        return clipInsertCommand;
    }

}
