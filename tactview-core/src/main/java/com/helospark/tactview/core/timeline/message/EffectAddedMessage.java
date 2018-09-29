package com.helospark.tactview.core.timeline.message;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class EffectAddedMessage {
    private String effectId;
    private String clipId;
    private TimelinePosition position;
    private StatelessEffect effect;

    public EffectAddedMessage(String effectId, String clipId, TimelinePosition position, StatelessEffect effect) {
        this.effectId = effectId;
        this.clipId = clipId;
        this.position = position;
        this.effect = effect;
    }

    public String getEffectId() {
        return effectId;
    }

    public String getClipId() {
        return clipId;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public StatelessEffect getEffect() {
        return effect;
    }

}
