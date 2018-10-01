package com.helospark.tactview.core.timeline.effect;

import com.helospark.tactview.core.timeline.TimelineInterval;

public class CreateEffectRequest {
    private TimelineInterval interval;
    private String effectId;

    public CreateEffectRequest(TimelineInterval interval, String effectId) {
        this.interval = interval;
        this.effectId = effectId;
    }

    public TimelineInterval getInterval() {
        return interval;
    }

    public String getEffectId() {
        return effectId;
    }

}
