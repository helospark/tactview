package com.helospark.tactview.core.timeline;

import java.util.List;
import java.util.UUID;

import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public abstract class StatelessEffect implements IntervalAware {
    String id;
    TimelineInterval interval;

    public StatelessEffect(TimelineInterval interval) {
        id = UUID.randomUUID().toString();
        this.interval = interval;
    }

    @Override
    public TimelineInterval getInterval() {
        return interval;
    }

    public String getId() {
        return id;
    }

    public abstract List<ValueProviderDescriptor> getValueProviders();

    public void setInterval(TimelineInterval timelineInterval) {
        this.interval = timelineInterval;
    }
}
