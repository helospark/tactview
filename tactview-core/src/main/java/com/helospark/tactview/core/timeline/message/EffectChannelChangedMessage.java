package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class EffectChannelChangedMessage implements AffectedModifiedIntervalAware {
    private List<TimelineInterval> affectedIntervals;
    private String id;
    private int newChannelIndex;

    public EffectChannelChangedMessage(String id, int newChannelIndex, TimelineInterval interval) {
        this.id = id;
        this.newChannelIndex = newChannelIndex;
        this.affectedIntervals = List.of(interval);
    }

    public String getId() {
        return id;
    }

    public int getNewChannelIndex() {
        return newChannelIndex;
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return affectedIntervals;
    }

    @Override
    public String toString() {
        return "EffectChannelChangedMessage [affectedIntervals=" + affectedIntervals + ", id=" + id + ", newChannelIndex=" + newChannelIndex + "]";
    }

}
