package com.helospark.tactview.core.timeline;

import java.util.UUID;

public class TimelineClip implements IntervalAware {
    private String id;
    private TimelineInterval interval;
    private TimelineClipType type;

    public TimelineClip(TimelineInterval interval, TimelineClipType type) {
        this.id = UUID.randomUUID().toString();
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

    public String getId() {
        return id;
    }

}
