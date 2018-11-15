package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.List;

import com.helospark.tactview.core.decoder.AudioMediaDecoder;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public abstract class AudibleTimelineClip extends TimelineClip {
    protected AudioMediaMetadata mediaMetadata;

    public AudibleTimelineClip(TimelineInterval interval, AudioMediaMetadata mediaMetadata) {
        super(interval, TimelineClipType.AUDIO);
        this.mediaMetadata = mediaMetadata;
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    protected List<ValueProviderDescriptor> getDescriptorsInternal() {
        return new ArrayList<>();
    }

    public AudioMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    public abstract AudioFrameResult requestAudioFrame(AudioRequest audioRequest);

    public abstract AudioMediaDecoder getMediaDecoder();
}
