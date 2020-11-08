package com.helospark.tactview.core.timeline.message;

import java.util.List;
import java.util.Optional;

import com.helospark.tactview.core.timeline.EffectAware;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public interface AbstractDescriptorsAddedMessage {

    public String getComponentId();

    public List<ValueProviderDescriptor> getDescriptors();

    public EffectAware getIntervalAware();

    public default Optional<String> getParentId() {
        return Optional.empty();
    }

}
