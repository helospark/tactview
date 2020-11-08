package com.helospark.tactview.core.timeline.message;

import java.util.List;
import java.util.Optional;

import com.helospark.tactview.core.timeline.EffectAware;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public class GraphComponentDescriptorsAdded implements AbstractDescriptorsAddedMessage {
    private String componentId;
    private List<ValueProviderDescriptor> descriptors;
    private EffectAware intervalAware;
    private String parentId;

    public GraphComponentDescriptorsAdded(String componentId, List<ValueProviderDescriptor> descriptors, EffectAware intervalAware, String parentId) {
        this.descriptors = descriptors;
        this.componentId = componentId;
        this.intervalAware = intervalAware;
        this.parentId = parentId;
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptors() {
        return descriptors;
    }

    @Override
    public String toString() {
        return "GraphComponentDescriptorsAdded [componentId=" + componentId + ", descriptors=" + descriptors + "]";
    }

    @Override
    public String getComponentId() {
        return componentId;
    }

    @Override
    public EffectAware getIntervalAware() {
        return intervalAware;
    }

    @Override
    public Optional<String> getParentId() {
        return Optional.of(parentId);
    }

}
