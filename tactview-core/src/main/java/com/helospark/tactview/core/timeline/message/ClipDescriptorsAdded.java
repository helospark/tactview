package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public class ClipDescriptorsAdded {
    private String clipId;
    private List<ValueProviderDescriptor> descriptors;

    public ClipDescriptorsAdded(String clipId, List<ValueProviderDescriptor> descriptors) {
        this.descriptors = descriptors;
        this.clipId = clipId;
    }

    public String getClipId() {
        return clipId;
    }

    public List<ValueProviderDescriptor> getDescriptors() {
        return descriptors;
    }

}
