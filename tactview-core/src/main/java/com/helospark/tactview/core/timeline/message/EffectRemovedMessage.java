package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class EffectRemovedMessage implements AffectedModifiedIntervalAware {
    private String effectId;
    private String clipId;
    private TimelineInterval interval;

    public EffectRemovedMessage(String effectId, String clipId, TimelineInterval interval) {
        this.effectId = effectId;
        this.clipId = clipId;
        this.interval = interval;
    }

    public String getEffectId() {
        return effectId;
    }

    public String getClipId() {
        return clipId;
    }

    @Override
    public String toString() {
        return "EffectRemovedMessage [effectId=" + effectId + ", clipId=" + clipId + ", interval=" + interval + "]";
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(interval);
    }

}
