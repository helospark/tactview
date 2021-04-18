package com.helospark.tactview.core.timeline;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.audioeffect.AudioEffectRequest;
import com.helospark.tactview.core.timeline.audioeffect.StatelessAudioEffect;
import com.helospark.tactview.core.util.MathUtil;

import sonic.Sonic;

public abstract class AudibleTimelineClip extends TimelineClip {
    protected AudioMediaMetadata mediaMetadata;

    public AudibleTimelineClip(TimelineInterval interval, AudioMediaMetadata mediaMetadata) {
        super(interval, TimelineClipType.AUDIO);
        this.mediaMetadata = mediaMetadata;
    }

    public AudibleTimelineClip(AudioMediaMetadata metadata, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(savedClip, loadMetadata);
        this.mediaMetadata = metadata;
    }

    public AudibleTimelineClip(AudibleTimelineClip soundClip, CloneRequestMetadata cloneRequestMetadata) {
        super(soundClip, cloneRequestMetadata);
        this.mediaMetadata = soundClip.mediaMetadata;
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    public AudioMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    public AudioFrameResult requestAudioFrame(AudioRequest audioRequest) {
        TimelinePosition relativePosition = audioRequest.getPosition().from(this.interval.getStartPosition());

        boolean hasTempoChange = timeScaleProvider.keyframesEnabled() || !MathUtil.fuzzyEquals(timeScaleProvider.getValueAt(audioRequest.getPosition()), 1.0);

        if (hasTempoChange) {
            TimelinePosition unscaledPosition = relativePosition.add(renderOffset);
            BigDecimal integrated = timeScaleProvider.integrate(renderOffset.toPosition(), unscaledPosition);
            TimelinePosition startPosition = renderOffset.toPosition().add(integrated);
            TimelineLength newLength = audioRequest.getLength().multiply(BigDecimal.valueOf(timeScaleProvider.getValueAt(audioRequest.getPosition())));

            audioRequest = AudioRequest.builderFrom(audioRequest)
                    .withPosition(startPosition)
                    .withLength(newLength)
                    .build();
        }

        AudioFrameResult result = requestAudioFrameInternal(audioRequest);

        if (hasTempoChange) {
            var newResult = applyTempoChange(result, audioRequest);
            result.free();
            result = newResult;
        }

        return applyEffects(relativePosition, result, audioRequest.isApplyEffects());
    }

    private AudioFrameResult applyTempoChange(AudioFrameResult result, AudioRequest audioRequest) {
        double tempo = timeScaleProvider.getValueAt(audioRequest.getPosition());
        int numberChannels = result.getChannels().size();

        int samplesInInput = result.getNumberSamples() * numberChannels;
        int expectedSamplesInOutput = (int) Math.ceil(samplesInInput * (1.0 / tempo)) + 1000;

        float[] samples = new float[Math.max(samplesInInput, expectedSamplesInOutput)];

        for (int i = 0; i < result.getNumberSamples(); ++i) {
            for (int j = 0; j < numberChannels; ++j) {
                samples[i * numberChannels + j] = result.getSampleAt(j, i) / (float) (1 << (result.getBytesPerSample() * 8));
            }
        }

        int actualOutputSamples = Sonic.changeFloatSpeed(samples, result.getNumberSamples(), (float) tempo, 1.0f, 1.0f, 1.0f, false, result.getSamplePerSecond(),
                result.getChannels().size());

        List<ByteBuffer> newChannels = new ArrayList<>();
        for (int i = 0; i < numberChannels; ++i) {
            newChannels.add(GlobalMemoryManagerAccessor.memoryManager.requestBuffer(actualOutputSamples * result.getBytesPerSample()));
        }
        AudioFrameResult newResult = new AudioFrameResult(newChannels, result.getSamplePerSecond(), result.getBytesPerSample());
        for (int i = 0; i < actualOutputSamples; ++i) {
            for (int j = 0; j < numberChannels; ++j) {
                newResult.setSampleAt(j, i, (int) (samples[i * numberChannels + j] * (1 << (result.getBytesPerSample() * 8))));
            }
        }

        return newResult;
    }

    protected abstract AudioFrameResult requestAudioFrameInternal(AudioRequest audioRequest);

    protected AudioFrameResult applyEffects(TimelinePosition relativePosition, AudioFrameResult frameResult, boolean applyEffects) {
        if (applyEffects) {
            List<StatelessAudioEffect> actualEffects = getEffectsAt(relativePosition, StatelessAudioEffect.class);

            for (StatelessAudioEffect effect : actualEffects) {

                if (effect.isEnabledAt(relativePosition)) {
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
        }
        return frameResult;
    }
}
