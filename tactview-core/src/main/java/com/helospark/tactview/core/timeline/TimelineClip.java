package com.helospark.tactview.core.timeline;

public class TimelineClip implements IntervalAware {
    private TimelineInterval interval;
    private TimelineClipType type;

    public TimelineClip(TimelineInterval interval, TimelineClipType type) {
        this.interval = interval;
        this.type = type;
    }

    @Override
    public TimelineInterval getInterval() {
        return interval;
    }

    public TimelineClipType getType() {
        return type;
    }

}
