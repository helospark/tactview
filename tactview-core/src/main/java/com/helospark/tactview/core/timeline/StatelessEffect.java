package com.helospark.tactview.core.timeline;

import java.util.List;
import java.util.UUID;

import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public abstract class StatelessEffect implements IntervalAware, IntervalSettable {
    protected String id;
    protected TimelineInterval interval;
    protected IntervalAware parentIntervalAware;

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

    @Override
    public void setInterval(TimelineInterval timelineInterval) {
        this.interval = timelineInterval;
    }

    public void setParentIntervalAware(IntervalAware parentIntervalAware) {
        this.parentIntervalAware = parentIntervalAware;
    }

    @Override
    public TimelineInterval getGlobalInterval() {
        return this.interval.butAddOffset(parentIntervalAware.getInterval().getStartPosition());
    }

    public void notifyAfterResize() {
    }

    public void notifyAfterInitialized() {

    }

}
