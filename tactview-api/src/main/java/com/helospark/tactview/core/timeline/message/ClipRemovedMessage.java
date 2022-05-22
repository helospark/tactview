package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class ClipRemovedMessage implements AffectedModifiedIntervalAware {
    private String elementId;
    private TimelineInterval originalInterval;
    private TimelineClip originalClip;

    public ClipRemovedMessage(String elementId, TimelineInterval originalInterval, TimelineClip originalClip) {
        this.elementId = elementId;
        this.originalInterval = originalInterval;
        this.originalClip = originalClip;
    }

    public String getElementId() {
        return elementId;
    }

    public TimelineClip getOriginalClip() {
        return originalClip;
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
