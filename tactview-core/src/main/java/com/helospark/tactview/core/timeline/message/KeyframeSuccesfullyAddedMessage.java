package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class KeyframeSuccesfullyAddedMessage implements AffectedModifiedIntervalAware {
    private String descriptorId;
    private TimelineInterval interval;

    public KeyframeSuccesfullyAddedMessage(String descriptorId, TimelineInterval globalInterval) {
        this.descriptorId = descriptorId;
        this.interval = globalInterval;
    }

    public String getDescriptorId() {
        return descriptorId;
    }

    public TimelineInterval getInterval() {
        return interval;
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(interval);
    }

}
