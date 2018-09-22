package com.helospark.tactview.core.timeline;

public class TimelineClip implements IntervalAware {
    private TimelineInterval interval;

    @Override
    public TimelineInterval getInterval() {
        return interval;
    }
}
