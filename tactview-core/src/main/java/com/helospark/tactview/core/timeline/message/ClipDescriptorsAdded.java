package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public class ClipDescriptorsAdded {
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

}
