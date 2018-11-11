package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.List;

import com.helospark.tactview.core.decoder.AudioMediaDecoder;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public abstract class AudibleTimelineClip extends TimelineClip {

    public AudibleTimelineClip(TimelineInterval interval) {
        super(interval, TimelineClipType.AUDIO);
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    protected List<ValueProviderDescriptor> getDescriptorsInternal() {
        return new ArrayList<>();
    }

    public abstract AudioFrameResult requestAudioFrame(AudioRequest audioRequest);

    public abstract AudioMediaDecoder getMediaMetadata();
}
