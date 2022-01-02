package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.helospark.tactview.core.timeline.ClipChannelIdPair;
import com.helospark.tactview.core.timeline.MoveClipRequest;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.message.NotificationMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.commands.UiCommand;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditMode;

public class ClipInsertCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;
    private MessagingService messagingService;
    private TimelineEditMode timelineEditMode;

    private TimelineClip insertInPlace;
    private List<String> clipIdsToInsert;

    private TimelinePosition distanceToMove;
    boolean success = false;
    List<ClipChannelIdPair> actuallyInsertedClips;
    TimelineInterval combinedInterval;
    List<Integer> channelIndices;
    boolean moveBackward;
    List<TimelineClip> clipsToMove;

    private ClipInsertCommand(Builder builder) {
        this.timelineManager = builder.timelineManager;
        this.messagingService = builder.messagingService;
        this.timelineEditMode = builder.timelineEditMode;
        this.insertInPlace = builder.insertInPlace;
        this.clipIdsToInsert = builder.clipIdsToInsert;
    }

    @Override
    public void execute() {
        synchronized (timelineManager.getFullLock()) {
            clipsToMove = timelineManager.resolveClipIdsWithAllLinkedClip(clipIdsToInsert);
            List<TimelineChannel> channels = clipsToMove.stream()
                    .flatMap(clip -> timelineManager.findChannelForClipId(clip.getId()).stream())
                    .collect(Collectors.toList());
            List<String> clipIds = clipsToMove.stream().map(a -> a.getId()).collect(Collectors.toList());
            TimelinePosition positionToInsertTo = insertInPlace.getInterval().getStartPosition();
            channelIndices = channels.stream()
                    .flatMap(a -> timelineManager.findChannelIndexByChannelId(a.getId()).stream())
                    .collect(Collectors.toList());
            boolean isThereACutAtClipPositions = channels.stream().allMatch(channel -> isThereACatAtPosition(channel, positionToInsertTo, clipIds));

            if (!isThereACutAtClipPositions) {
                success = false;
                messagingService.sendMessage(new NotificationMessage("Cannot insert", "Not all channels have cut at position", NotificationMessage.Level.WARNING));
                return;
            }
            if (clipsToMove.size() == 0) {
                messagingService.sendMessage(new NotificationMessage("Cannot insert", "No clips found to move", NotificationMessage.Level.WARNING));
                return;
            }

            combinedInterval = findCombinedInterval(clipsToMove);
            distanceToMove = combinedInterval.getEndPosition().subtract(combinedInterval.getStartPosition());
            moveBackward = true;
            if (combinedInterval.getStartPosition().subtract(positionToInsertTo).isLessThan(TimelinePosition.ofZero())) {
                distanceToMove = distanceToMove.negate();
                moveBackward = false;
            }

            actuallyInsertedClips = removeClipsToInsert(clipsToMove);
            TimelinePosition newPos = moveBackClipsBehind(positionToInsertTo, channelIndices, distanceToMove, moveBackward, combinedInterval);
            insertClipsAt(newPos, combinedInterval, actuallyInsertedClips);
            success = true;
        }
    }

    private void insertClipsAt(TimelinePosition positionToInsertTo, TimelineInterval combinedInterval, List<ClipChannelIdPair> removedClips) {
        for (var removedClip : removedClips) {
            var clip = removedClip.clip;

            TimelineInterval originalInterval = clip.getInterval();

            TimelinePosition relativePositionWithinCombinedInterval = originalInterval.getStartPosition().subtract(combinedInterval.getStartPosition());
            TimelinePosition newPosition = positionToInsertTo.add(relativePositionWithinCombinedInterval);
            clip.setInterval(originalInterval.butMoveStartPostionTo(newPosition));

            TimelineChannel channel = timelineManager.findChannelWithId(removedClip.channel).get();
            timelineManager.addClip(channel, clip);
        }
    }

    private TimelinePosition moveBackClipsBehind(TimelinePosition positionToInsertTo, List<Integer> channelIndices, TimelinePosition distanceToMove, boolean moveBackward,
            TimelineInterval combinedInterval) {
        TreeSet<TimelineClip> clipsToMoveBack = new TreeSet<>();
        if (timelineEditMode.equals(TimelineEditMode.SINGLE_CHANNEL_RIPPLE)) {
            clipsToMoveBack = timelineManager.findClipsRightFromPositionAndOnChannelIgnoring(positionToInsertTo, channelIndices, List.of());
        } else if (timelineEditMode.equals(TimelineEditMode.ALL_CHANNEL_RIPPLE)) {
            clipsToMoveBack = timelineManager.findClipsRightFromPositionAndOnChannelIgnoring(positionToInsertTo, timelineManager.getAllChannelIndices(), List.of());
        } else {
            if (moveBackward) {
                TreeSet<TimelineClip> clipsRightOf = timelineManager.findClipsRightFromPositionAndOnChannelIgnoring(positionToInsertTo, channelIndices, clipIdsToInsert);
                TreeSet<TimelineClip> clipsLeftOf = timelineManager.findClipLeftOfPositionIncludingPartialOnChannels(combinedInterval.getStartPosition(), channelIndices, clipIdsToInsert);
                clipsRightOf.retainAll(clipsLeftOf);
                clipsToMoveBack = clipsRightOf;
            } else {
                TreeSet<TimelineClip> clipsRightOf = timelineManager.findClipsRightFromPositionAndOnChannelIgnoring(combinedInterval.getEndPosition(), channelIndices, clipIdsToInsert);
                TreeSet<TimelineClip> clipsLeftOf = timelineManager.findClipLeftOfPositionOnChannels(positionToInsertTo.add(insertInPlace.getInterval().getLength()), channelIndices,
                        clipIdsToInsert);
                clipsRightOf.retainAll(clipsLeftOf);
                clipsToMoveBack = clipsRightOf;
            }
        }
        if (clipsToMoveBack.size() > 0) {
            TimelineClip firstClipToMove = clipsToMoveBack.stream().findFirst().get();
            MoveClipRequest moveClipRequest = MoveClipRequest.builder()
                    .withAdditionalClipIds(clipsToMoveBack.stream().map(a -> a.getId()).collect(Collectors.toList()))
                    .withAdditionalSpecialPositions(List.of())
                    .withClipId(firstClipToMove.getId())
                    .withEnableJumpingToSpecialPosition(false)
                    .withMoreMoveExpected(false)
                    .withNewChannelId(timelineManager.findChannelForClipId(firstClipToMove.getId()).get().getId())
                    .withNewPosition(firstClipToMove.getInterval().getStartPosition().add(distanceToMove))
                    .build();
            timelineManager.moveClip(moveClipRequest);
        }
        if (moveBackward || clipsToMoveBack.size() == 0) {
            return positionToInsertTo;
        } else {
            return findCombinedInterval(new ArrayList<>(clipsToMoveBack)).getEndPosition();
        }
    }

    private List<ClipChannelIdPair> removeClipsToInsert(List<TimelineClip> clipsToMove) {
        // TODO: ripple if set
        List<ClipChannelIdPair> clipsRemoved = new ArrayList<>();
        for (var clip : clipsToMove) {
            TimelineChannel channel = timelineManager.findChannelForClipId(clip.getId()).get();
            clipsRemoved.add(new ClipChannelIdPair(clip, channel.getId()));
            timelineManager.removeClip(clip.getId());
        }
        return clipsRemoved;
    }

    private TimelineInterval findCombinedInterval(List<TimelineClip> clipsToMove) {
        TimelinePosition startPosition = clipsToMove.get(0).getInterval().getStartPosition();
        TimelinePosition endPosition = clipsToMove.get(0).getInterval().getEndPosition();

        for (var clip : clipsToMove) {
            TimelineInterval interval = clip.getInterval();
            if (interval.getStartPosition().isLessThan(startPosition)) {
                startPosition = interval.getStartPosition();
            }
            if (interval.getEndPosition().isGreaterThan(endPosition)) {
                endPosition = interval.getEndPosition();
            }
        }
        return new TimelineInterval(startPosition, endPosition);
    }

    private boolean isThereACatAtPosition(TimelineChannel channel, TimelinePosition startPosition, List<String> clipIds) {
        Integer channelIndex = timelineManager.findChannelIndexByChannelId(channel.getId()).get();
        TreeSet<TimelineClip> right = timelineManager.findClipsRightFromPositionAndOnChannelIgnoring(startPosition, List.of(channelIndex), clipIds);
        TreeSet<TimelineClip> left = timelineManager.findClipLeftOfPositionOnChannels(startPosition, List.of(channelIndex), clipIds);
        return !right.equals(left);
    }

    @Override
    public void revert() {
        if (success) {
            TimelineInterval newCombinedInterval = findCombinedInterval(clipsToMove);
            List<ClipChannelIdPair> removedClips = removeClipsToInsert(actuallyInsertedClips.stream().map(a -> a.clip).collect(Collectors.toList()));
            moveBackClipsBehind(combinedInterval.getStartPosition(), channelIndices, distanceToMove.negate(), !moveBackward, newCombinedInterval);
            insertClipsAt(combinedInterval.getStartPosition(), new TimelineInterval(insertInPlace.getInterval().getStartPosition(), distanceToMove.toLength()), removedClips);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private TimelineManagerAccessor timelineManager;
        private MessagingService messagingService;
        private TimelineEditMode timelineEditMode;
        private TimelineClip insertInPlace;
        private List<String> clipIdsToInsert = Collections.emptyList();

        private Builder() {
        }

        public Builder withTimelineManager(TimelineManagerAccessor timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public Builder withMessagingService(MessagingService messagingService) {
            this.messagingService = messagingService;
            return this;
        }

        public Builder withTimelineEditMode(TimelineEditMode timelineEditMode) {
            this.timelineEditMode = timelineEditMode;
            return this;
        }

        public Builder withInsertInPlace(TimelineClip insertInPlace) {
            this.insertInPlace = insertInPlace;
            return this;
        }

        public Builder withClipIdsToInsert(List<String> clipIdsToInsert) {
            this.clipIdsToInsert = clipIdsToInsert;
            return this;
        }

        public ClipInsertCommand build() {
            return new ClipInsertCommand(this);
        }
    }

}
