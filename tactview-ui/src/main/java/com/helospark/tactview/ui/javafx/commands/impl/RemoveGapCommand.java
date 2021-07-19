package com.helospark.tactview.ui.javafx.commands.impl;

import static com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditMode.NORMAL;

import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.helospark.tactview.core.timeline.MoveClipRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.message.NotificationMessage;
import com.helospark.tactview.core.timeline.message.NotificationMessage.Level;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.commands.UiCommand;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditMode;

public class RemoveGapCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;
    private UiMessagingService messagingService;

    private TimelinePosition position;
    private int channelIndex;
    private TimelineEditMode timelineEditMode;

    private List<TimelineClip> clipsToMove;
    private TimelinePosition jump;
    private boolean successful = false;

    public RemoveGapCommand(TimelineManagerAccessor timelineManager, UiMessagingService messagingService, TimelinePosition position, int channelIndex, TimelineEditMode timelineEditMode) {
        this.timelineManager = timelineManager;
        this.messagingService = messagingService;
        this.position = position;
        this.channelIndex = channelIndex;
        this.timelineEditMode = timelineEditMode;
    }

    @Override
    public void execute() {
        if (timelineEditMode.equals(NORMAL)) {
            List<String> excludedClips = List.of();
            TreeSet<TimelineClip> clipsToRight = timelineManager.findClipsRightFromPositionAndOnChannelIgnoring(position, List.of(channelIndex), excludedClips);

            if (clipsToRight.isEmpty()) {
                messagingService.sendMessage(new NotificationMessage("No gap at position", "No gap at position", Level.ERROR));
                return;
            }

            TimelineClip clipToMove = clipsToRight.first();
            clipsToMove = timelineManager.resolveClipIdsWithAllLinkedClip(List.of(clipToMove.getId()));

            List<Integer> channelIds = clipsToMove.stream()
                    .flatMap(clip -> timelineManager.findChannelIndexForClipId(clip.getId()).stream())
                    .collect(Collectors.toList());

            Optional<TimelineClip> intersectingClip = channelIds.stream()
                    .flatMap(channelId -> timelineManager.getChannels().get(channelId).getDataAt(position).stream())
                    .findFirst();
            if (intersectingClip.isPresent()) {
                messagingService.sendMessage(new NotificationMessage("No gap at position", "No gap at position", Level.ERROR));
                return;
            }

            jump = calculateRelativeJumpForChannels(excludedClips, channelIds);

            if (jump.equals(TimelinePosition.ofZero())) {
                messagingService.sendMessage(new NotificationMessage("No gap at position", "No gap at position", Level.ERROR));
                return;
            }

            MoveClipRequest moveClipRequest = MoveClipRequest.builder()
                    .withAdditionalClipIds(clipsToMove.stream().map(a -> a.getId()).collect(Collectors.toList()))
                    .withAdditionalSpecialPositions(List.of())
                    .withClipId(clipsToMove.get(0).getId())
                    .withEnableJumpingToSpecialPosition(false)
                    .withMoreMoveExpected(false)
                    .withNewChannelId(timelineManager.findChannelForClipId(clipsToMove.get(0).getId()).get().getId())
                    .withNewPosition(clipsToMove.get(0).getInterval().getStartPosition().subtract(jump))
                    .build();
            timelineManager.moveClip(moveClipRequest);

            successful = true;
        }

    }

    private TimelinePosition calculateRelativeJumpForChannels(List<String> excludedClips, List<Integer> channelIds) {
        TimelinePosition relativeJumpPosition = TimelinePosition.ofZero();
        boolean initialized = false;
        for (var channelId : channelIds) {
            TreeSet<TimelineClip> clipsToLeft = timelineManager.findClipLeftOfPositionOnChannels(position, List.of(channelId), excludedClips);
            TimelinePosition leftPosition;
            if (!clipsToLeft.isEmpty()) {
                TimelineClip leftestClip = clipsToLeft.last();
                leftPosition = leftestClip.getInterval().getEndPosition();
            } else {
                leftPosition = TimelinePosition.ofZero();
            }
            TreeSet<TimelineClip> clipsToRight = timelineManager.findClipsRightFromPositionAndOnChannelIgnoring(position, List.of(channelId), excludedClips);
            if (!clipsToRight.isEmpty()) {
                TimelineClip firstClipToRight = clipsToRight.first();
                TimelinePosition currentJump = firstClipToRight.getInterval().getStartPosition().subtract(leftPosition);

                if (currentJump.isLessThan(relativeJumpPosition) || !initialized) {
                    relativeJumpPosition = currentJump;
                    initialized = true;
                }
            }
        }
        return relativeJumpPosition;
    }

    @Override
    public void revert() {
        if (!successful) {
            return;
        }

        MoveClipRequest moveClipRequest = MoveClipRequest.builder()
                .withAdditionalClipIds(clipsToMove.stream().map(a -> a.getId()).collect(Collectors.toList()))
                .withAdditionalSpecialPositions(List.of())
                .withClipId(clipsToMove.get(0).getId())
                .withEnableJumpingToSpecialPosition(false)
                .withMoreMoveExpected(false)
                .withNewChannelId(timelineManager.findChannelForClipId(clipsToMove.get(0).getId()).get().getId())
                .withNewPosition(clipsToMove.get(0).getInterval().getStartPosition().add(jump))
                .build();
        timelineManager.moveClip(moveClipRequest);
    }

    @Override
    public boolean isRevertable() {
        return successful;
    }

    @Override
    public String toString() {
        return "RemoveGapCommand [timelineManager=" + timelineManager + ", messagingService=" + messagingService + ", position=" + position + ", channelIndex=" + channelIndex + ", timelineEditMode="
                + timelineEditMode + ", clipsToMove=" + clipsToMove + ", jump=" + jump + ", successful=" + successful + "]";
    }

}
