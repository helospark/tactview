package com.helospark.tactview.core.timeline.audioeffect.balance;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.audioeffect.AudioEffectRequest;
import com.helospark.tactview.core.timeline.audioeffect.StatelessAudioEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

public class ChannelBalanceAudioEffect extends StatelessAudioEffect {
    private List<DoubleProvider> channelVolumeProviders;
    private ProjectRepository projectRepository;

    public ChannelBalanceAudioEffect(TimelineInterval interval, ProjectRepository projectRepository) {
        super(interval);
        this.projectRepository = projectRepository;
    }

    public ChannelBalanceAudioEffect(ChannelBalanceAudioEffect volumeAudioEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(volumeAudioEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(volumeAudioEffect, this, cloneRequestMetadata);
    }

    public ChannelBalanceAudioEffect(JsonNode node, LoadMetadata loadMetadata, ProjectRepository projectRepository) {
        super(node, loadMetadata);
        this.projectRepository = projectRepository;
    }

    @Override
    protected AudioFrameResult applyEffectInternal(AudioEffectRequest request) {
        AudioFrameResult input = request.getInput();
        AudioFrameResult result = AudioFrameResult.sameSizeAndFormatAs(input);

        for (int channel = 0; channel < input.getChannels().size(); ++channel) {
            double volumeMultiplier = channelVolumeProviders.get(channel).getValueAt(request.getEffectPosition(), request.getEvaluationContext());
            for (int sampleIndex = 0; sampleIndex < input.getNumberSamples(); ++sampleIndex) {
                int sample = input.getSampleAt(channel, sampleIndex);
                sample *= volumeMultiplier;
                result.setSampleAt(channel, sampleIndex, sample);
            }
        }

        return result;
    }

    @Override
    protected void initializeValueProviderInternal() {
        channelVolumeProviders = new ArrayList<>();
        for (int i = 0; i < projectRepository.getNumberOfChannels(); ++i) {
            channelVolumeProviders.add(new DoubleProvider(0.0, 2.0, new MultiKeyframeBasedDoubleInterpolator(1.0)));
        }
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        List<ValueProviderDescriptor> result = new ArrayList<>();
        for (int i = 0; i < channelVolumeProviders.size(); ++i) {
            result.add(ValueProviderDescriptor.builder()
                    .withKeyframeableEffect(channelVolumeProviders.get(i))
                    .withName("channel " + i)
                    .build());
        }

        return result;
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new ChannelBalanceAudioEffect(this, cloneRequestMetadata);
    }

}
