package com.helospark.tactview.core.timeline;

import java.util.Optional;

public class TimelineChannel {
    private NonIntersectingIntervalList<TimelineClip> clips = new NonIntersectingIntervalList<>();

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
}
