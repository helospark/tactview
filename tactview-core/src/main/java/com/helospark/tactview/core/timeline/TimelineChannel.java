package com.helospark.tactview.core.timeline;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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

        if (canAddResourceAt(newInterval)) {
            clipToMove.setInterval(newInterval);
            clips.addInterval(clipToMove);
            return true;
        } else {
            return false;
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

}
