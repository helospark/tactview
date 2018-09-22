package com.helospark.tactview.core.timeline;

public class TimelineChannel {
    private NonIntersectingIntervalList<TimelineClip> clips = new NonIntersectingIntervalList<>();

    public boolean canAddResourceAt(TimelinePosition position, TimelineLength length) {
        return clips.canAddInterval(new TimelineInterval(position, length));
    }
}
