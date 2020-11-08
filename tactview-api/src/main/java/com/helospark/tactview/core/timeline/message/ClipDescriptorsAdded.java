package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.EffectAware;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public class ClipDescriptorsAdded implements AbstractDescriptorsAddedMessage {
    private String clipId;
    private List<ValueProviderDescriptor> descriptors;
    private TimelineClip clip;

    public ClipDescriptorsAdded(String clipId, List<ValueProviderDescriptor> descriptors, TimelineClip clip) {
        this.descriptors = descriptors;
        this.clipId = clipId;
        this.clip = clip;
    }

    public String getClipId() {
        return clipId;
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptors() {
        return descriptors;
    }

    public TimelineClip getClip() {
        return clip;
    }

    @Override
    public String toString() {
        return "ClipDescriptorsAdded [clipId=" + clipId + ", descriptors=" + descriptors + ", clip=" + clip + "]";
    }

    @Override
    public String getComponentId() {
        return getClipId();
    }

    @Override
    public EffectAware getIntervalAware() {
        return clip;
    }
}
