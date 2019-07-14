package com.helospark.tactview.core.util.messaging;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;

public interface AffectedModifiedIntervalAware {

    public List<TimelineInterval> getAffectedIntervals();

}
