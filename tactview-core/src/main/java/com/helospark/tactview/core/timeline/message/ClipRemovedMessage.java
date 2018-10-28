package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class ClipRemovedMessage implements AffectedModifiedIntervalAware {
    private String elementId;
    private TimelineInterval originalInterval;

    public ClipRemovedMessage(String elementId, TimelineInterval originalInterval) {
        this.elementId = elementId;
        this.originalInterval = originalInterval;
    }

    public String getElementId() {
        return elementId;
    }

    @Override
    public String toString() {
        return "ClipRemovedMessage [elementId=" + elementId + "]";
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(originalInterval);
    }

}
