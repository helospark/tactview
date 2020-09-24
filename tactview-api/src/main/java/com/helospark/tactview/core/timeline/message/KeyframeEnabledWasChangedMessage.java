package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class KeyframeEnabledWasChangedMessage extends AbstractKeyframeChangedMessage implements AffectedModifiedIntervalAware {
    private final TimelineInterval interval;
    private final boolean useKeyframes;
    private final String containerId;
    private final String keyframeableEffectId;

    public KeyframeEnabledWasChangedMessage(String containerId, String keyframeableEffectId, boolean useKeyframes, TimelineInterval interval) {
        this.useKeyframes = useKeyframes;
        this.keyframeableEffectId = keyframeableEffectId;
        this.interval = interval;
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }

    @Override
    public TimelineInterval getInterval() {
        return interval;
    }

    public boolean isUseKeyframes() {
        return useKeyframes;
    }

    public String getKeyframeableEffectId() {
        return keyframeableEffectId;
    }

    @Override
    public String getDescriptorId() {
        return containerId;
    }

    @Override
    public String getContainingElementId() {
        return containerId;
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(interval);
    }

    @Override
    public String toString() {
        return "KeyframeEnabledWasChangedMessage [interval=" + interval + ", useKeyframes=" + useKeyframes + ", containerId=" + containerId + ", keyframeableEffectId=" + keyframeableEffectId + "]";
    }

}
