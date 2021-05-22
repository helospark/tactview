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
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.threading.SingleThreadedRenderable;
import com.helospark.tactview.core.util.MathUtil;

import sonic.Sonic;

public abstract class AudibleTimelineClip extends TimelineClip implements SingleThreadedRenderable {
    protected AudioMediaMetadata mediaMetadata;

    protected Sonic sonic;

    protected DoubleProvider pitchShiftProvider;

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
        this.pitchShiftProvider = soundClip.pitchShiftProvider.deepClone();
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

        boolean hasTempoChange = hasSonicPostProcessingChange();

        TimelinePosition startPosition = super.calculatePositionInClipSpaceTo(relativePosition, false);
        TimelineLength newLength = audioRequest.getLength().multiply(BigDecimal.valueOf(timeScaleProvider.getValueAt(audioRequest.getPosition())));

        AudioRequest newAudioRequest = AudioRequest.builderFrom(audioRequest)
                .withPosition(startPosition)
                .withLength(newLength)
                .build();

        AudioFrameResult result = requestAudioFrameInternal(newAudioRequest);

        if (hasTempoChange) {
            var newResult = applyTempoChange(result, newAudioRequest);
            result.free();
            result = newResult;
        }

        return applyEffects(startPosition.subtract(renderOffset), result, newAudioRequest.isApplyEffects());
    }

    private boolean hasSonicPostProcessingChange() {
        return timeScaleProvider.keyframesEnabled() || !MathUtil.fuzzyEquals(timeScaleProvider.getValueAt(TimelinePosition.ofZero()), 1.0) ||
                pitchShiftProvider.keyframesEnabled() || !MathUtil.fuzzyEquals(pitchShiftProvider.getValueAt(TimelinePosition.ofZero()), 1.0);
    }

    private AudioFrameResult applyTempoChange(AudioFrameResult result, AudioRequest audioRequest) {
        double tempo = timeScaleProvider.getValueAt(audioRequest.getPosition());
        double pitchShift = pitchShiftProvider.getValueAt(audioRequest.getPosition());
        int numberChannels = result.getChannels().size();

        int samplesInInput = result.getNumberSamples() * numberChannels;
        int sampleRate = result.getSamplePerSecond();

        float[] samples = new float[samplesInInput];

        for (int i = 0; i < result.getNumberSamples(); ++i) {
            for (int j = 0; j < numberChannels; ++j) {
                samples[i * numberChannels + j] = result.getNormalizedSampleAt(j, i);
            }
        }

        if (sonic == null || sonic.getSampleRate() != sampleRate || sonic.getNumChannels() != numberChannels) {
            sonic = new Sonic(sampleRate, numberChannels);
            sonic.setVolume(1.0f);
            sonic.setChordPitch(false);
            sonic.setQuality(0);
            sonic.setRate(1.0f);
        }
        sonic.setSpeed((float) tempo);
        sonic.setPitch((float) pitchShift);

        sonic.writeFloatToStream(samples, result.getNumberSamples());

        int actualOutputSamples = sonic.samplesAvailable();
        float[] outputSamples = new float[actualOutputSamples * numberChannels];
        actualOutputSamples = sonic.readFloatFromStream(outputSamples, actualOutputSamples);

        List<ByteBuffer> newChannels = new ArrayList<>();
        for (int i = 0; i < numberChannels; ++i) {
            newChannels.add(GlobalMemoryManagerAccessor.memoryManager.requestBuffer(actualOutputSamples * result.getBytesPerSample()));
        }
        AudioFrameResult newResult = new AudioFrameResult(newChannels, sampleRate, result.getBytesPerSample());
        for (int i = 0; i < actualOutputSamples; ++i) {
            for (int j = 0; j < numberChannels; ++j) {
                newResult.setNormalizedSampleAt(j, i, outputSamples[i * numberChannels + j]);
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

    @Override
    public void onStartRender() {
        sonic = null;
    }

    @Override
    public boolean isSequentialRenderEnabled() {
        return hasSonicPostProcessingChange();
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();
        pitchShiftProvider = new DoubleProvider(0.01, 4.0, new BezierDoubleInterpolator(1.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor pitchShiftProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(pitchShiftProvider)
                .withName("Pitch shift")
                .withGroup("speed")
                .build();

        result.add(pitchShiftProviderDescriptor);

        return result;
    }

    @Override
    public boolean effectSupported(StatelessEffect effect) {
        return effect instanceof StatelessAudioEffect;
    }
}
