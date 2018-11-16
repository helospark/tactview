package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.List;

import com.helospark.tactview.core.decoder.AudioMediaDecoder;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.audioeffect.AudioEffectRequest;
import com.helospark.tactview.core.timeline.audioeffect.StatelessAudioEffect;
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

    public AudioFrameResult requestAudioFrame(AudioRequest audioRequest) {
        TimelinePosition relativePosition = audioRequest.getPosition().from(this.interval.getStartPosition());

        AudioFrameResult result = requestAudioFrameInternal(audioRequest);

        return applyEffects(relativePosition, result, audioRequest.isApplyEffects());
    }

    protected abstract AudioFrameResult requestAudioFrameInternal(AudioRequest audioRequest);

    public abstract AudioMediaDecoder getMediaDecoder();

    protected AudioFrameResult applyEffects(TimelinePosition relativePosition, AudioFrameResult frameResult, boolean applyEffects) {
        if (applyEffects) {
            List<StatelessAudioEffect> actualEffects = getEffectsAt(relativePosition, StatelessAudioEffect.class);

            for (StatelessAudioEffect effect : actualEffects) {
                AudioEffectRequest request = AudioEffectRequest.builder()
                        .withClipPosition(relativePosition)
                        .withEffectPosition(relativePosition.from(effect.getInterval().getStartPosition()))
                        .withInput(frameResult)
                        .build();

                AudioFrameResult appliedEffectsResult = effect.applyEffect(request);

                frameResult.getChannels()
                        .stream()
                        .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a));

                frameResult = appliedEffectsResult;
            }
        }
        return frameResult;
    }
}
