package com.helospark.tactview.core.timeline.audioeffect.volume;

import java.util.List;

import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.audioeffect.AudioEffectRequest;
import com.helospark.tactview.core.timeline.audioeffect.StatelessAudioEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

public class VolumeAudioEffect extends StatelessAudioEffect {
    private DoubleProvider volumeProvider;

    public VolumeAudioEffect(TimelineInterval interval) {
        super(interval);
    }

    public VolumeAudioEffect(VolumeAudioEffect volumeAudioEffect) {
        super(volumeAudioEffect);
        ReflectionUtil.copyOrCloneFieldFromTo(volumeAudioEffect, this);
    }

    @Override
    protected AudioFrameResult applyEffectInternal(AudioEffectRequest request) {
        AudioFrameResult input = request.getInput();
        AudioFrameResult result = AudioFrameResult.sameSizeAndFormatAs(input);
        double volumeMultiplier = volumeProvider.getValueAt(request.getEffectPosition());

        for (int channel = 0; channel < input.getChannels().size(); ++channel) {
            for (int sampleIndex = 0; sampleIndex < input.getNumberSamples(); ++sampleIndex) {
                int sample = input.getSampleAt(channel, sampleIndex);
                sample *= volumeMultiplier;
                result.setSampleAt(channel, sampleIndex, sample);
            }
        }

        return result;
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        volumeProvider = new DoubleProvider(0, 10, new MultiKeyframeBasedDoubleInterpolator(0.6));

        ValueProviderDescriptor heightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(volumeProvider)
                .withName("Volume")
                .build();

        return List.of(heightDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new VolumeAudioEffect(this);
    }

}
