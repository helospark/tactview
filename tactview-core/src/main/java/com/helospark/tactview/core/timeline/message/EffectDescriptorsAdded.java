package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.IntervalAware;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public class EffectDescriptorsAdded {
    private String effectId;
    private List<ValueProviderDescriptor> descriptors;
    private IntervalAware intervalAware;

    public EffectDescriptorsAdded(String effectId, List<ValueProviderDescriptor> descriptors, IntervalAware intervalAware) {
        this.descriptors = descriptors;
        this.effectId = effectId;
        this.intervalAware = intervalAware;
    }

    public String getEffectId() {
        return effectId;
    }

    public List<ValueProviderDescriptor> getDescriptors() {
        return descriptors;
    }

    public IntervalAware getIntervalAware() {
        return intervalAware;
    }

}
