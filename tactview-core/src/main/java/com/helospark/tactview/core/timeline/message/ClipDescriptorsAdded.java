package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.IntervalAware;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public class ClipDescriptorsAdded {
    private String clipId;
    private List<ValueProviderDescriptor> descriptors;
    private IntervalAware clip;

    public ClipDescriptorsAdded(String clipId, List<ValueProviderDescriptor> descriptors, IntervalAware clip) {
        this.descriptors = descriptors;
        this.clipId = clipId;
        this.clip = clip;
    }

    public String getClipId() {
        return clipId;
    }

    public List<ValueProviderDescriptor> getDescriptors() {
        return descriptors;
    }

    public IntervalAware getClip() {
        return clip;
    }

    @Override
    public String toString() {
        return "ClipDescriptorsAdded [clipId=" + clipId + ", descriptors=" + descriptors + ", clip=" + clip + "]";
    }

}
