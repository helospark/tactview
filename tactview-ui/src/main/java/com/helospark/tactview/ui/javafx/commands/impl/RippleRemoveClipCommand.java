package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.MoveClipRequest;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.message.NotificationMessage;
import com.helospark.tactview.core.timeline.message.NotificationMessage.Level;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.commands.UiCommand;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditMode;

public class RippleRemoveClipCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;
    private MessagingService messagingService;

    private List<String> clipIds;
    private TimelineEditMode timelineEditMode;

    private MultiValuedMap<TimelineChannel, TimelineClip> removedClips = new ArrayListValuedHashMap<>();
    private List<TimelineClip> movedClips = new ArrayList<>();
    private TimelinePosition moveDistance;
    private boolean rippleDeletePerformed = false;

    public RippleRemoveClipCommand(TimelineManagerAccessor timelineManager, MessagingService messagingService, List<String> clipIds, TimelineEditMode timelineEditMode) {
        this.timelineManager = timelineManager;
        this.messagingService = messagingService;
        this.clipIds = clipIds;
        this.timelineEditMode = timelineEditMode;
    }

    @Override
    public void execute() {
        List<TimelineClip> clipsToRemove = clipIds.stream()
                .flatMap(a -> timelineManager.findClipById(a).stream())
                .collect(Collectors.toList());

        if (clipsToRemove.isEmpty()) {
            return;
        }

        TimelineLength maxLengthOfClip = TimelineLength.ofZero();
        for (var clip : clipsToRemove) {
            if (clip.getInterval().getLength().greaterThan(maxLengthOfClip)) {
                maxLengthOfClip = clip.getInterval().getLength();
            }
        }

        TimelinePosition positionToDelete = clipsToRemove.get(0).getInterval().getStartPosition();
        for (var clip : clipsToRemove) {
            if (clip.getInterval().getStartPosition().isLessThan(positionToDelete)) {
                positionToDelete = clip.getInterval().getStartPosition();
            }
        }

        List<TimelineClip> clipsToMoveBack = findClipsToRipple(clipsToRemove, positionToDelete);

        if (!checkIfRippleDeleteCanBePerformed(clipsToRemove, maxLengthOfClip, clipsToMoveBack)) {
            return;
        }

        for (var clip : clipsToRemove) {
            removedClips.put(timelineManager.findChannelForClipId(clip.getId()).get(), clip.cloneClip(CloneRequestMetadata.fullCopy()));
            timelineManager.removeClip(clip.getId());
        }

        if (clipsToMoveBack.size() > 0) {
            moveDistance = maxLengthOfClip.toPosition();

            MoveClipRequest moveClipRequest = MoveClipRequest.builder()
                    .withAdditionalClipIds(clipsToMoveBack.stream().map(a -> a.getId()).collect(Collectors.toList()))
                    .withAdditionalSpecialPositions(List.of())
                    .withClipId(clipsToMoveBack.get(0).getId())
                    .withEnableJumpingToSpecialPosition(false)
                    .withMoreMoveExpected(false)
                    .withNewChannelId(timelineManager.findChannelForClipId(clipsToMoveBack.get(0).getId()).get().getId())
                    .withNewPosition(clipsToMoveBack.get(0).getInterval().getStartPosition().subtract(moveDistance))
                    .build();
            timelineManager.moveClip(moveClipRequest);

            clipsToMoveBack.stream()
                    .map(clip -> clip.cloneClip(CloneRequestMetadata.fullCopy()))
                    .forEach(clip -> movedClips.add(clip));

        }
        rippleDeletePerformed = true;
    }

    private List<TimelineClip> findClipsToRipple(List<TimelineClip> clipsToRemove, TimelinePosition positionToDelete) {
        List<TimelineClip> clipsToMoveBack = new ArrayList<>(timelineManager.findClipsRightFromPositionIgnoring(positionToDelete, clipIds));

        if (timelineEditMode.equals(TimelineEditMode.SINGLE_CHANNEL_RIPPLE)) {
            Set<String> channelsToRipple = findAllChannelsForClips(clipsToRemove);

            clipsToMoveBack = clipsToMoveBack.stream()
                    .filter(clip -> channelsToRipple.contains(getChannelIdForClip(clip)))
                    .collect(Collectors.toList());
        }
        return clipsToMoveBack;
    }

    private String getChannelIdForClip(TimelineClip clip) {
        return timelineManager.findChannelForClipId(clip.getId()).get().getId();
    }

    private Set<String> findAllChannelsForClips(List<TimelineClip> clipsToRemove) {
        return clipsToRemove.stream()
                .flatMap(clip -> timelineManager.findChannelForClipId(clip.getId()).stream())
                .map(channel -> channel.getId())
                .collect(Collectors.toSet());
    }

    private boolean checkIfRippleDeleteCanBePerformed(List<TimelineClip> clipsToRemove, TimelineLength maxLengthOfClip, List<TimelineClip> clipsToMoveBack) {
        List<String> excludedIdsInCheck = new ArrayList<>();
        clipsToMoveBack.stream()
                .forEach(a -> excludedIdsInCheck.add(a.getId()));
        clipsToRemove.stream()
                .forEach(a -> excludedIdsInCheck.add(a.getId()));
        Set<Integer> conflictingChannels = new HashSet<>();
        for (var clip : clipsToMoveBack) {
            TimelineChannel channel = timelineManager.findChannelForClipId(clip.getId()).get();
            int channelIndex = timelineManager.findChannelIndexForClipId(clip.getId()).get();
            TimelineInterval newInterval = clip.getInterval().butAddOffset(maxLengthOfClip.toPosition().negate());
            boolean canAddResource = channel.canAddResourceAtExcluding(newInterval, excludedIdsInCheck);
            if (!canAddResource) {
                conflictingChannels.add(channelIndex);
            }
        }
        boolean canPerformRippleDelete = conflictingChannels.isEmpty();
        if (!canPerformRippleDelete) {
            messagingService.sendMessage(new NotificationMessage("Unable to ripple delete", "Clips in channel " + conflictingChannels + " cannot be moved back", Level.ERROR));
        }
        return canPerformRippleDelete;
    }

    @Override
    public void revert() {
        if (!rippleDeletePerformed) {
            return;
        }
        MoveClipRequest moveClipRequest = MoveClipRequest.builder()
                .withAdditionalClipIds(movedClips.stream().map(a -> a.getId()).collect(Collectors.toList()))
                .withAdditionalSpecialPositions(List.of())
                .withClipId(movedClips.get(0).getId())
                .withEnableJumpingToSpecialPosition(false)
                .withMoreMoveExpected(false)
                .withNewChannelId(timelineManager.findChannelForClipId(movedClips.get(0).getId()).get().getId())
                .withNewPosition(movedClips.get(0).getInterval().getStartPosition().add(moveDistance))
                .build();
        timelineManager.moveClip(moveClipRequest);

        for (var entry : removedClips.entries()) {
            timelineManager.addClip(entry.getKey(), entry.getValue());
        }

    }

    @Override
    public void preDestroy() {
        for (var entry : removedClips.entries()) {
            entry.getValue().preDestroy();
        }
    }

    @Override
    public boolean isRevertable() {
        return rippleDeletePerformed;
    }

}
