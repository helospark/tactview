package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class EffectAddedMessage implements AffectedModifiedIntervalAware {
    private String effectId;
    private String clipId;
    private TimelinePosition position;
    private StatelessEffect effect;
    private int newEffectChannelId;
    private TimelineInterval interval;

    public EffectAddedMessage(String effectId, String clipId, TimelinePosition position, StatelessEffect effect, int newEffectChannelId, TimelineInterval interval) {
        this.effectId = effectId;
        this.clipId = clipId;
        this.position = position;
        this.effect = effect;
        this.newEffectChannelId = newEffectChannelId;
        this.interval = interval;
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

    public int getNewEffectChannelId() {
        return newEffectChannelId;
    }

    @Override
    public String toString() {
        return "EffectAddedMessage [effectId=" + effectId + ", clipId=" + clipId + ", position=" + position + ", effect=" + effect + ", newEffectChannelId=" + newEffectChannelId + "]";
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(interval);
    }

}
