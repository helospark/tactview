package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

public class TimelineChannel {
    private NonIntersectingIntervalList<TimelineClip> clips = new NonIntersectingIntervalList<>();
    private String id = UUID.randomUUID().toString();

    private boolean disabled = false;
    private boolean mute = false;

    private Object fullChannelLock = new Object();

    public TimelineChannel(JsonNode savedChannel) {
        this.id = savedChannel.get("id").asText();
        this.disabled = savedChannel.get("disabled").asBoolean(false);
        this.mute = savedChannel.get("mute").asBoolean(false);
    }

    public TimelineChannel() {

    }

    public Map<String, Object> generateSavedContent() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("disabled", disabled);
        result.put("mute", mute);
        List<Object> serializedClips = new ArrayList<>();
        for (TimelineClip clip : clips) {
            serializedClips.add(clip.generateSavedContent());
        }
        result.put("clips", serializedClips);
        return result;
    }

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
        synchronized (fullChannelLock) {
            clips.addInterval(clip);
        }
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
        synchronized (fullChannelLock) {
            TimelineClip clip = findClipById(addedClipId)
                    .orElseThrow(() -> new IllegalArgumentException("Channel does not contain " + addedClipId));
            clips.remove(clip);
        }
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

        synchronized (fullChannelLock) {

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

        synchronized (fullChannelLock) {
            return clips.resize(clip, newInterval);
        }
    }

    public List<TimelineInterval> findSpecialPositionsAround(TimelinePosition position, TimelineLength length, List<String> ignoredIds) {
        TimelineInterval inInterval = new TimelineInterval(position.subtract(length), length.multiply(2));
        List<TimelineInterval> specialPointsFromClips = clips.computeIntersectingIntervals(inInterval)
                .stream()
                .filter(a -> !ignoredIds.contains(a.getId()))
                .map(a -> a.getInterval())
                .collect(Collectors.toList());
        List<TimelineInterval> otherSpecialPoints = Collections.singletonList(new TimelineInterval(TimelinePosition.ofZero(), TimelineLength.ofZero()));
        // We could add effects
        ArrayList<TimelineInterval> result = new ArrayList<>(specialPointsFromClips);
        result.addAll(otherSpecialPoints);

        List<TimelineInterval> effectSpecialPoints = clips.stream()
                .flatMap(a -> a.interesectingEffects(new TimelineInterval(position.from(a.getInterval().getStartPosition()).subtract(length), length.multiply(2))))
                .filter(a -> !ignoredIds.contains(a.getId()))
                .map(a -> a.getGlobalInterval())
                .collect(Collectors.toList());

        result.addAll(effectSpecialPoints);

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

    public TimelinePosition findPositionWhereIntervalWithLengthCanBeInserted(TimelineLength length) {
        return clips.get(clips.size() - 1).getInterval().getEndPosition(); // tmp implementation
    }

    public Object getFullChannelLock() {
        return fullChannelLock;
    }

    public boolean canAddResourceAtExcluding(TimelineInterval clipNewPosition, Collection<String> linkedClipIds) {
        return linkedClipIds.stream()
                .anyMatch(id -> canAddResourceAtExcluding(clipNewPosition, id));
    }

    public TimelinePosition findMaximumEndPosition() {
        TimelinePosition endPosition = TimelinePosition.ofZero();

        for (var clip : clips) {
            if (clip.getInterval().getEndPosition().isGreaterThan(endPosition)) {
                endPosition = clip.getInterval().getEndPosition();
            }
        }

        return endPosition;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isMute() {
        return mute;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

}
