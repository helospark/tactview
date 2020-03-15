package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class ChannelMovedMessage implements AffectedModifiedIntervalAware {
    private int newIndex;
    private int originalIndex;
    private List<TimelineInterval> affectedIntervals;

    public ChannelMovedMessage(int newIndex, int originalIndex, List<TimelineInterval> affectedIntervals) {
        this.newIndex = newIndex;
        this.originalIndex = originalIndex;
        this.affectedIntervals = affectedIntervals;
    }

    public int getNewIndex() {
        return newIndex;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return affectedIntervals;
    }

}
