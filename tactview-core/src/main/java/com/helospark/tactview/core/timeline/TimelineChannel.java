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
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.SaveMetadata;

public class TimelineChannel {
    private NonIntersectingIntervalList<TimelineClip> clips = new NonIntersectingIntervalList<>();
    private String id = UUID.randomUUID().toString();

    private boolean disabled = false;
    private boolean mute = false;

    private final Object fullChannelLock = new Object();

    public TimelineChannel(JsonNode savedChannel) {
        this.id = savedChannel.get("id").asText();
        this.disabled = savedChannel.get("disabled").asBoolean(false);
        this.mute = savedChannel.get("mute").asBoolean(false);
    }

    public TimelineChannel(TimelineChannel originalChannel, CloneRequestMetadata cloneRequestMetadata) {
        if (cloneRequestMetadata.isDeepCloneId()) {
            this.id = originalChannel.id;
        } else {
            this.id = UUID.randomUUID().toString();
        }
        this.disabled = originalChannel.disabled;
        this.mute = originalChannel.mute;

        this.clips = new NonIntersectingIntervalList<>();
        for (TimelineClip clip : originalChannel.clips) {
            this.clips.addInterval(clip.cloneClip(cloneRequestMetadata));
        }
    }

    public TimelineChannel() {

    }

    public Map<String, Object> generateSavedContent(SaveMetadata saveMetadata) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("disabled", disabled);
        result.put("mute", mute);
        List<Object> serializedClips = new ArrayList<>();
        for (TimelineClip clip : clips) {
            serializedClips.add(clip.generateSavedContent(saveMetadata));
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
        TimelineInterval newInterval = clip.getIntervalAfterRescaleTo(left, position);

        synchronized (fullChannelLock) {
            if (clips.canAddIntervalAtExcluding(newInterval, List.of(clip))) {
                clip.resize(left, newInterval);
                return clips.resize(clip, newInterval);
            } else {
                return false;
            }
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

    public boolean canAddResourceAtExcluding(TimelineInterval interval, Collection<String> excludeClipId) {
        List<TimelineClip> excludedClips = excludeClipId.stream()
                .flatMap(a -> findClipById(a).stream())
                .collect(Collectors.toList());
        return clips.canAddIntervalAtExcluding(interval, excludedClips) && interval.getStartPosition().isGreaterOrEqualToThan(TimelinePosition.ofZero());
    }

    public boolean canAddResourceAtExcludingClips(TimelineInterval interval, List<TimelineClip> excludedClips) {
        return clips.canAddIntervalAtExcluding(interval, excludedClips) && interval.getStartPosition().isGreaterOrEqualToThan(TimelinePosition.ofZero());
    }

    public List<String> getAllClipId() {
        return clips.stream()
                .map(clip -> clip.getId())
                .collect(Collectors.toList());
    }

    public NonIntersectingIntervalList<TimelineClip> getAllClips() {
        return clips;
    }

    public TimelinePosition findPositionWhereIntervalWithLengthCanBeInserted(TimelineLength length) {
        return clips.get(clips.size() - 1).getInterval().getEndPosition(); // tmp implementation
    }

    public Object getFullChannelLock() {
        return fullChannelLock;
    }

    //    public boolean canAddResourceAtExcluding(TimelineInterval clipNewPosition, Collection<String> linkedClipIds) {
    //        return linkedClipIds.stream()
    //                .anyMatch(id -> canAddResourceAtExcluding(clipNewPosition, id));
    //    }

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

    public int findMaximumVideoBitRate() {
        int maxBitRate = 0;
        for (var clip : clips) {
            if (clip instanceof VideoClip) {
                VisualMediaMetadata metadata = ((VideoClip) clip).getMediaMetadata();
                if (metadata instanceof VideoMetadata) {
                    int bitRate = ((VideoMetadata) metadata).getBitRate();
                    if (bitRate > maxBitRate) {
                        maxBitRate = bitRate;
                    }
                }
            }
        }

        return maxBitRate;
    }

    public int findMaximumAudioBitRate() {
        int maxBitRate = 0;
        for (var clip : clips) {
            if (clip instanceof AudibleTimelineClip) {
                AudioMediaMetadata metadata = ((AudibleTimelineClip) clip).getMediaMetadata();
                int bitRate = (int) metadata.getBitRate();
                if (bitRate > maxBitRate) {
                    maxBitRate = bitRate;
                }
            }
        }

        return maxBitRate;
    }

    public Optional<TimelineClip> findFirstClipToLeft(String clipId) {
        TimelineClip previous = null;
        for (var clip : clips) {
            if (clip.getId().equals(clipId)) {
                return Optional.ofNullable(previous);
            }
            previous = clip;
        }
        return Optional.empty();
    }

    public Optional<TimelineClip> findFirstClipToRight(String clipId) {
        int i = 0;
        for (; i < clips.size(); ++i) {
            if (clips.get(i).getId().equals(clipId)) {
                break;
            }
        }
        if (i + 1 >= clips.size()) {
            return Optional.empty();
        } else {
            return Optional.of(clips.get(i + 1));
        }
    }

    public TimelineChannel cloneChannel(CloneRequestMetadata metadata) {
        return new TimelineChannel(this, metadata);
    }

    public boolean areIntervalsIntersecting() {
        synchronized (fullChannelLock) {
            for (int i = 0; i < clips.size() - 1; ++i) {
                if (clips.get(i).getInterval().intersects(clips.get(i + 1).getInterval())) {
                    return true;
                }
            }
            return false;
        }
    }

}
