package com.helospark.tactview.core.timeline.effect;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class CreateEffectRequest {
    private TimelinePosition position;
    private String effectId;

    public CreateEffectRequest(TimelinePosition position, String effectId) {
        this.position = position;
        this.effectId = effectId;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public String getEffectId() {
        return effectId;
    }

}
