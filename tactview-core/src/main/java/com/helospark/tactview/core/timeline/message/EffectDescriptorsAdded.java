package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public class EffectDescriptorsAdded {
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

    public List<ValueProviderDescriptor> getDescriptors() {
        return descriptors;
    }

    public StatelessEffect getEffect() {
        return effect;
    }

}
