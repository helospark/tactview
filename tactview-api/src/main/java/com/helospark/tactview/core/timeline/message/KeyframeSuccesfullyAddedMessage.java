package com.helospark.tactview.core.timeline.message;

import java.util.List;
import java.util.Optional;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class KeyframeSuccesfullyAddedMessage extends AbstractKeyframeChangedMessage implements AffectedModifiedIntervalAware {
    private final String descriptorId;
    private final TimelineInterval interval;
    private final String containingElementId;
    private Optional<String> parentElementId = Optional.empty();

    public KeyframeSuccesfullyAddedMessage(String descriptorId, TimelineInterval globalInterval, String containingElementId) {
        this.descriptorId = descriptorId;
        this.interval = globalInterval;
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

    public void setParentElementId(Optional<String> parentElementId) {
        this.parentElementId = parentElementId;
    }

    @Override
    public Optional<String> getParentElementId() {
        return parentElementId;
    }

    @Override
    public String toString() {
        return "KeyframeSuccesfullyAddedMessage [descriptorId=" + descriptorId + ", interval=" + interval + ", containingElementId=" + containingElementId + "]";
    }

}
