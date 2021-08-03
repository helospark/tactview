package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.MoveClipRequest;
import com.helospark.tactview.core.timeline.ResizeClipRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.commands.UiCommand;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditMode;

public class ClipResizedCommand implements UiCommand {
    private TimelineManagerAccessor timelineManager;

    private List<String> clipIds;
    private TimelinePosition position;
    private boolean left;

    private TimelinePosition originalPosition;
    private TimelineInterval originalInterval;
    private Set<MovedClip> clipsToMove = Set.of();
    private TimelinePosition lengthToJump;
    private TimelinePosition relativeMove;

    private boolean revertable;

    private boolean useSpecialPoints;
    private boolean moreResizeExpected;
    private TimelineLength maximumJumpLength;
    private TimelineLength minimumSize;

    private TimelineEditMode timelineEditMode;

    @Generated("SparkTools")
    private ClipResizedCommand(Builder builder) {
        this.timelineManager = builder.timelineManager;
        this.clipIds = builder.clipIds;
        this.position = builder.position;
        this.left = builder.left;
        this.originalPosition = builder.originalPosition;
        this.revertable = builder.revertable;
        this.useSpecialPoints = builder.useSpecialPoints;
        this.maximumJumpLength = builder.maximumJumpLength;
        this.moreResizeExpected = builder.moreResizeExpected;
        this.minimumSize = builder.minimumSize;
        this.timelineEditMode = builder.timelineEditMode;
        this.relativeMove = builder.relativeMove;
        this.originalInterval = builder.originalInterval;

        if (relativeMove == null) {
            relativeMove = position.subtract(originalPosition);
        }
    }

    @Override
    public void execute() {
        synchronized (timelineManager.getFullLock()) {
            boolean isAnyRippleModeEnabled = !timelineEditMode.equals(TimelineEditMode.NORMAL);
            if (isAnyRippleModeEnabled) {
                List<Integer> channels;
                if (timelineEditMode.equals(TimelineEditMode.SINGLE_CHANNEL_RIPPLE)) {
                    channels = new ArrayList<>(timelineManager.findChannelIndicesForClips(clipIds));
                } else {
                    channels = timelineManager.getAllChannelIndices();
                }
                TimelineInterval intervalOfFirstClip = timelineManager.findClipById(clipIds.get(0)).get().getInterval();
                TimelinePosition ripplePosition;
                if (left) {
                    ripplePosition = intervalOfFirstClip.getStartPosition();
                } else {
                    ripplePosition = intervalOfFirstClip.getEndPosition();
                }
                clipsToMove = timelineManager.findClipsRightFromPositionAndOnChannelIgnoring(ripplePosition, channels, clipIds)
                        .stream()
                        .map(a -> new MovedClip(a, timelineManager.findChannelForClipId(a.getId()).get().getId()))
                        .collect(Collectors.toSet());

            }

            TimelinePosition newPosition = null;
            for (int i = 0; i < clipIds.size(); ++i) {
                String clipId = clipIds.get(i);
                TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("No clip found"));
                TimelineInterval originalIntervalInternal = clip.getInterval();

                List<String> ignoredSuggestions = new ArrayList<>();
                ignoredSuggestions.addAll(clipIds);
                clipsToMove.stream()
                        .map(a -> a.clip.getId())
                        .forEach(a -> ignoredSuggestions.add(a));

                TimelinePosition positionToUse;
                boolean leftRippleModePositionEnabled = left && isAnyRippleModeEnabled && relativeMove != null;
                if (i == 0) {
                    if (leftRippleModePositionEnabled) {
                        positionToUse = clip.getInterval().getStartPosition().add(relativeMove);
                    } else {
                        positionToUse = position;
                    }
                } else {
                    positionToUse = newPosition;
                }
                ResizeClipRequest request = ResizeClipRequest.builder()
                        .withClip(clip)
                        .withLeft(left)
                        .withUseSpecialPoints(i == 0 && useSpecialPoints && !leftRippleModePositionEnabled)
                        .withPosition(positionToUse)
                        .withMaximumJumpLength(maximumJumpLength)
                        .withMoreResizeExpected(moreResizeExpected)
                        .withMinimumSize(minimumSize)
                        .withIgnoredSpecialSuggestionClips(ignoredSuggestions)
                        .withIgnoreIntersection(clipsToMove.stream().map(a -> a.clip).collect(Collectors.toList()))
                        .withKeepLeftSideOfClipAtSamePlace(leftRippleModePositionEnabled) // TODO: it's not ideal that the ripple logic is split between 2 places
                        .build();

                timelineManager.resizeClip(request);

                if (i == 0) {
                    if (leftRippleModePositionEnabled) {
                        newPosition = positionToUse; // No special position enabled, so it's ok
                    } else {
                        newPosition = left ? clip.getUnmodifiedInterval().getStartPosition() : clip.getUnmodifiedInterval().getEndPosition();
                    }
                    lengthToJump = clip.getInterval().getEndPosition().subtract(originalInterval.getEndPosition());
                }
            }

            if (clipsToMove.size() > 0) {
                TimelineClip firstClipToMove = clipsToMove.stream().findFirst().get().clip;
                MoveClipRequest moveClipRequest = MoveClipRequest.builder()
                        .withAdditionalClipIds(clipsToMove.stream().map(a -> a.clip.getId()).collect(Collectors.toList()))
                        .withAdditionalSpecialPositions(List.of())
                        .withClipId(firstClipToMove.getId())
                        .withEnableJumpingToSpecialPosition(false)
                        .withMoreMoveExpected(false)
                        .withNewChannelId(timelineManager.findChannelForClipId(firstClipToMove.getId()).get().getId())
                        .withNewPosition(firstClipToMove.getInterval().getStartPosition().add(lengthToJump))
                        .build();
                timelineManager.moveClip(moveClipRequest);
            }
        }
    }

    @Override
    public void revert() {
        boolean isAnyRippleModeEnabled = !timelineEditMode.equals(TimelineEditMode.NORMAL);
        TimelinePosition lengthToJumpToUse = lengthToJump;
        for (var clipId : clipIds) {
            TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("No clip found"));
            TimelinePosition previousPosition = originalPosition;

            boolean wasLeftRipple = left && isAnyRippleModeEnabled;

            TimelinePosition positionToUse = previousPosition;
            if (wasLeftRipple) {
                TimelineLength intervalSizeDiff = clip.getGlobalInterval().getLength().subtract(originalInterval.getLength());
                positionToUse = positionToUse.add(intervalSizeDiff.toPosition());
                lengthToJumpToUse = intervalSizeDiff.toPosition().negate();
            } else {
                lengthToJumpToUse = originalInterval.getEndPosition().subtract(clip.getInterval().getEndPosition());
            }

            ResizeClipRequest request = ResizeClipRequest.builder()
                    .withClip(clip)
                    .withLeft(left)
                    .withUseSpecialPoints(false)
                    .withPosition(positionToUse)
                    .withKeepLeftSideOfClipAtSamePlace(wasLeftRipple)
                    .withIgnoreIntersection(clipsToMove.stream().map(a -> a.clip).collect(Collectors.toList()))
                    .build();

            timelineManager.resizeClip(request);
        }

        if (clipsToMove.size() > 0) {
            TimelineClip firstClipToMove = clipsToMove.stream().findFirst().get().clip;
            MoveClipRequest moveClipRequest = MoveClipRequest.builder()
                    .withAdditionalClipIds(clipsToMove.stream().map(a -> a.clip.getId()).collect(Collectors.toList()))
                    .withAdditionalSpecialPositions(List.of())
                    .withClipId(firstClipToMove.getId())
                    .withEnableJumpingToSpecialPosition(false)
                    .withMoreMoveExpected(false)
                    .withNewChannelId(timelineManager.findChannelForClipId(firstClipToMove.getId()).get().getId())
                    .withNewPosition(firstClipToMove.getInterval().getStartPosition().add(lengthToJumpToUse))
                    .build();
            timelineManager.moveClip(moveClipRequest);
        }
    }

    @Override
    public boolean isRevertable() {
        return revertable;
    }

    @Override
    public String toString() {
        return "ClipResizedCommand [timelineManager=" + timelineManager + ", clipIds=" + clipIds + ", position=" + position + ", left=" + left + ", originalPosition=" + originalPosition
                + ", clipsToMove=" + clipsToMove + ", lengthToJump=" + lengthToJump + ", revertable=" + revertable + ", useSpecialPoints=" + useSpecialPoints + ", moreResizeExpected="
                + moreResizeExpected + ", maximumJumpLength=" + maximumJumpLength + ", minimumSize=" + minimumSize + ", timelineEditMode=" + timelineEditMode + "]";
    }

    static class MovedClip {
        TimelineClip clip;
        String channel;

        public MovedClip(TimelineClip clip, String channel) {
            this.clip = clip;
            this.channel = channel;
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof MovedClip)) {
                return false;
            }
            MovedClip castOther = (MovedClip) other;
            return Objects.equals(clip, castOther.clip) && Objects.equals(channel, castOther.channel);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clip, channel);
        }

    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineManagerAccessor timelineManager;
        private List<String> clipIds;
        private TimelinePosition position;
        private boolean left;
        private TimelinePosition originalPosition;
        private TimelineInterval originalInterval;
        private TimelinePosition relativeMove;
        private boolean revertable;
        private boolean useSpecialPoints;
        private boolean moreResizeExpected;
        private TimelineLength maximumJumpLength;
        private TimelineLength minimumSize;
        private TimelineEditMode timelineEditMode = TimelineEditMode.NORMAL;

        private Builder() {
        }

        public Builder withTimelineManager(TimelineManagerAccessor timelineManager) {
            this.timelineManager = timelineManager;
            return this;
        }

        public Builder withClipIds(List<String> clipIds) {
            this.clipIds = clipIds;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withLeft(boolean left) {
            this.left = left;
            return this;
        }

        public Builder withOriginalPosition(TimelinePosition originalPosition) {
            this.originalPosition = originalPosition;
            return this;
        }

        public Builder withOriginalInterval(TimelineInterval originalInterval) {
            this.originalInterval = originalInterval;
            return this;
        }

        public Builder withRelativeMove(TimelinePosition relativeMove) {
            this.relativeMove = relativeMove;
            return this;
        }

        public Builder withRevertable(boolean revertable) {
            this.revertable = revertable;
            return this;
        }

        public Builder withUseSpecialPoints(boolean useSpecialPoints) {
            this.useSpecialPoints = useSpecialPoints;
            return this;
        }

        public Builder withMoreResizeExpected(boolean moreResizeExpected) {
            this.moreResizeExpected = moreResizeExpected;
            return this;
        }

        public Builder withMaximumJumpLength(TimelineLength maximumJumpLength) {
            this.maximumJumpLength = maximumJumpLength;
            return this;
        }

        public Builder withMinimumSize(TimelineLength minimumSize) {
            this.minimumSize = minimumSize;
            return this;
        }

        public Builder withTimelineEditMode(TimelineEditMode timelineEditMode) {
            this.timelineEditMode = timelineEditMode;
            return this;
        }

        public ClipResizedCommand build() {
            return new ClipResizedCommand(this);
        }
    }
}
