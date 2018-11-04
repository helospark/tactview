package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TimelineChannel {
    private NonIntersectingIntervalList<TimelineClip> clips = new NonIntersectingIntervalList<>();
    private String id = UUID.randomUUID().toString();

    public boolean canAddResourceAt(TimelinePosition position, TimelineLength length) {
        return clips.canAddInterval(new TimelineInterval(position, length));
    }

    public boolean canAddResourceAt(TimelineInterval interval) {
        return clips.canAddInterval(interval);
    }

    public Optional<TimelineClip> getDataAt(TimelinePosition position) {
        return clips.getElementWithIntervalContainingPoint(position);
    }

    public void addResource(TimelineClip clip) {
        clips.addInterval(clip);
    }

    public Optional<TimelineClip> findClipById(String id) {
        for (TimelineClip clip : clips) {
            if (clip.getId().equals(id)) {
                return Optional.of(clip);
            }
        }
        return Optional.empty();
    }

    public void removeClip(String addedClipId) {
        TimelineClip clip = findClipById(addedClipId)
                .orElseThrow(() -> new IllegalArgumentException("Channel does not contain " + addedClipId));
        clips.remove(clip);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof TimelineChannel)) {
            return false;
        }
        TimelineChannel castOther = (TimelineChannel) other;
        return Objects.equals(id, castOther.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean moveClip(String clipId, TimelinePosition newPosition) {
        TimelineClip clipToMove = findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("Cannot find clip"));
        TimelineInterval originalInterval = clipToMove.getInterval();
        TimelineInterval newInterval = new TimelineInterval(newPosition, originalInterval.getLength());

        clips.remove(clipToMove);

        boolean success = false;

        if (canAddResourceAt(newInterval)) {
            clipToMove.setInterval(newInterval);
            success = true;
        } else {
            success = false;
        }
        clips.addInterval(clipToMove);
        return success;
    }

    public Optional<TimelineClip> findClipContainingEffect(String effectId) {
        for (TimelineClip clip : clips) {
            Optional<StatelessEffect> effect = clip.getEffect(effectId);
            if (effect.isPresent()) {
                return Optional.of(clip);
            }
        }
        return Optional.empty();
    }

    public boolean resizeClip(TimelineClip clip, boolean left, TimelinePosition position) {
        TimelineInterval originalInterval = clip.getInterval();
        TimelineInterval newInterval = left ? originalInterval.butWithStartPosition(position) : originalInterval.butWithEndPosition(position);

        return clips.resize(clip, newInterval);
    }

    public void generateSavedContent() {
        for (TimelineClip clip : clips) {
            clip.generateSavedContent();
        }
    }

    public List<TimelineInterval> findSpecialPositionsAround(TimelinePosition position, TimelineLength length, String excludeClip) {
        TimelineInterval inInterval = new TimelineInterval(position.subtract(length), length.multiply(2));
        List<TimelineInterval> specialPointsFromClips = clips.computeIntersectingIntervals(inInterval)
                .stream()
                .filter(a -> !a.getId().equals(excludeClip))
                .map(a -> a.getInterval())
                .collect(Collectors.toList());
        List<TimelineInterval> otherSpecialPoints = Collections.singletonList(new TimelineInterval(TimelinePosition.ofZero(), TimelineLength.ofZero()));
        // We could add effects
        ArrayList<TimelineInterval> result = new ArrayList<>(specialPointsFromClips);
        result.addAll(otherSpecialPoints);
        // ...
        return result;
    }

    public boolean canAddResourceAtExcluding(TimelineInterval interval, String excludeClipId) {
        Optional<TimelineClip> optionalExcludedClip = findClipById(excludeClipId);
        if (optionalExcludedClip.isPresent()) {
            // Should be part of list
            TimelineClip excludedClip = optionalExcludedClip.get();
            clips.remove(excludedClip);
            boolean result = clips.canAddInterval(interval);
            clips.addInterval(excludedClip);
            return result;
        } else {
            return clips.canAddInterval(interval);
        }
    }

    public List<String> getAllClipId() {
        return clips.stream()
                .map(clip -> clip.getId())
                .collect(Collectors.toList());
    }

}
