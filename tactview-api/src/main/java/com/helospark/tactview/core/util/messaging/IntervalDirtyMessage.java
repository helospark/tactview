package com.helospark.tactview.core.util.messaging;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;

public class IntervalDirtyMessage implements AffectedModifiedIntervalAware {
    private List<TimelineInterval> affectedInterval;

    public IntervalDirtyMessage(List<TimelineInterval> affectedInterval) {
        this.affectedInterval = affectedInterval;
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return affectedInterval;
    }

}
