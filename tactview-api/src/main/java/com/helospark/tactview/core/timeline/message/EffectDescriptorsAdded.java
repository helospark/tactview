package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.EffectAware;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public class EffectDescriptorsAdded implements AbstractDescriptorsAddedMessage {
    private String effectId;
    private List<ValueProviderDescriptor> descriptors;
    private StatelessEffect effect;

    public EffectDescriptorsAdded(String effectId, List<ValueProviderDescriptor> descriptors, StatelessEffect intervalAware) {
        this.descriptors = descriptors;
        this.effectId = effectId;
        this.effect = intervalAware;
    }

    public String getEffectId() {
        return effectId;
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptors() {
        return descriptors;
    }

    public StatelessEffect getEffect() {
        return effect;
    }

    @Override
    public String toString() {
        return "EffectDescriptorsAdded [effectId=" + effectId + ", descriptors=" + descriptors + ", effect=" + effect + "]";
    }

    @Override
    public String getComponentId() {
        return getEffectId();
    }

    @Override
    public EffectAware getIntervalAware() {
        return effect;
    }
}
