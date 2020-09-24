package com.helospark.tactview.core.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.message.AbstractKeyframeChangedMessage;

public class InterpolatorChangedMessage extends AbstractKeyframeChangedMessage {
    private String descriptorId;
    private TimelineInterval interval;
    private String containingElementId;

    public InterpolatorChangedMessage(String descriptorId, TimelineInterval interval, String containingElementId) {
        this.descriptorId = descriptorId;
        this.interval = interval;
        this.containingElementId = containingElementId;
    }

    @Override
    public String getDescriptorId() {
        return descriptorId;
    }

    @Override
    public TimelineInterval getInterval() {
        return interval;
    }

    @Override
    public String getContainingElementId() {
        return containingElementId;
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(interval);
    }

}
