package com.helospark.tactview.core.timeline.audioeffect.normalize;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.audioeffect.AudioEffectRequest;
import com.helospark.tactview.core.timeline.audioeffect.StatelessAudioEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.longprocess.LongProcessAudibleImagePushAware;
import com.helospark.tactview.core.timeline.longprocess.LongProcessAudioPushRequest;
import com.helospark.tactview.core.timeline.longprocess.LongProcessAware;
import com.helospark.tactview.core.timeline.longprocess.LongProcessDuplaceRequestStrategy;
import com.helospark.tactview.core.timeline.longprocess.LongProcessFrameRequest;
import com.helospark.tactview.core.timeline.longprocess.LongProcessRequestor;

public class NormalizeAudioEffect extends StatelessAudioEffect implements LongProcessAware, LongProcessAudibleImagePushAware {
    private LongProcessRequestor longProcessRequestor;

    private double multiplier = 1.0;

    private int currentMaximum;
    private int currentSampleSize = 1;

    public NormalizeAudioEffect(TimelineInterval interval) {
        super(interval);
    }

    public NormalizeAudioEffect(NormalizeAudioEffect volumeAudioEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(volumeAudioEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(volumeAudioEffect, this);
    }

    public NormalizeAudioEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    @Override
    protected AudioFrameResult applyEffectInternal(AudioEffectRequest request) {
        AudioFrameResult input = request.getInput();
        AudioFrameResult result = AudioFrameResult.sameSizeAndFormatAs(input);

        for (int channel = 0; channel < input.getChannels().size(); ++channel) {
            for (int sampleIndex = 0; sampleIndex < input.getNumberSamples(); ++sampleIndex) {
                int sample = input.getSampleAt(channel, sampleIndex);
                sample *= multiplier;
                result.setSampleAt(channel, sampleIndex, sample);
            }
        }

        return result;
    }

    @Override
    public void initializeValueProvider() {
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        return List.of();
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new NormalizeAudioEffect(this, cloneRequestMetadata);
    }

    @Override
    public void beginLongPush() {
        currentMaximum = 0;
    }

    @Override
    public void longProcessImage(LongProcessAudioPushRequest pushRequest) {
        AudioFrameResult frame = pushRequest.getFrame();
        currentSampleSize = frame.getBytesPerSample();

        for (int channel = 0; channel < frame.getChannels().size(); ++channel) {
            for (int sampleIndex = 0; sampleIndex < frame.getNumberSamples(); ++sampleIndex) {
                int sample = Math.abs(frame.getSampleAt(channel, sampleIndex));
                if (sample > currentMaximum) {
                    currentMaximum = sample;
                }
            }
        }
    }

    @Override
    public void endToPushLongImages() {
        multiplier = Math.pow(2.0, currentSampleSize * 8) / currentMaximum;
    }

    @Override
    public void setLongProcessRequestor(LongProcessRequestor longProcessRequestor) {
        this.longProcessRequestor = longProcessRequestor;
    }

    @Override
    public void notifyAfterInitialized() {
        LongProcessFrameRequest longProcessFrameRequest = LongProcessFrameRequest.builder()
                .withDuplaceRequestStrategy(LongProcessDuplaceRequestStrategy.ONLY_KEEP_LATEST_REQUEST)
                .build();
        longProcessRequestor.requestAudioFrames(this, longProcessFrameRequest);
    }

}
