package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public class EffectDescriptorsAdded {
    private String effectId;
    private List<ValueProviderDescriptor> descriptors;

    public EffectDescriptorsAdded(String effectId, List<ValueProviderDescriptor> descriptors) {
        this.descriptors = descriptors;
        this.effectId = effectId;
    }

    public String getEffectId() {
        return effectId;
    }

    public List<ValueProviderDescriptor> getDescriptors() {
        return descriptors;
    }

}
