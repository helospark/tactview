package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.EffectAware;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public interface AbstractDescriptorsAddedMessage {

    public String getComponentId();

    public List<ValueProviderDescriptor> getDescriptors();

    public EffectAware getIntervalAware();

}
