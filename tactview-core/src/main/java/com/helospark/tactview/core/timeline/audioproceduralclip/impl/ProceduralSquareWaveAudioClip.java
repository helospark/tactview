package com.helospark.tactview.core.timeline.audioproceduralclip.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.AudioRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl.ConstantInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.proceduralclip.audio.ProceduralAudioClip;
import com.helospark.tactview.core.util.ReflectionUtil;

public class ProceduralSquareWaveAudioClip extends ProceduralAudioClip {
    private DoubleProvider onTimeProvider;
    private DoubleProvider offTimeProvider;
    private DoubleProvider amplitudeProvider;

    public ProceduralSquareWaveAudioClip(TimelineInterval interval, AudioMediaMetadata mediaMetadata) {
        super(interval, mediaMetadata);
    }

    public ProceduralSquareWaveAudioClip(ProceduralSquareWaveAudioClip soundClip, CloneRequestMetadata cloneRequestMetadata) {
        super(soundClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(soundClip, this, cloneRequestMetadata);
    }

    public ProceduralSquareWaveAudioClip(AudioMediaMetadata metadata, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(metadata, savedClip, loadMetadata);
    }

    @Override
    protected AudioFrameResult requestAudioFrameInternal(AudioRequest audioRequest) {
        double amplitude = amplitudeProvider.getValueAt(audioRequest.getPosition());

        BigDecimal positionSecond = audioRequest.getPosition().getSeconds();
        BigDecimal onTime = BigDecimal.valueOf(Math.pow(10, BigDecimal.valueOf(onTimeProvider.getValueAt(audioRequest.getPosition())).doubleValue()));
        BigDecimal offTime = BigDecimal.valueOf(Math.pow(10, BigDecimal.valueOf(offTimeProvider.getValueAt(audioRequest.getPosition())).doubleValue()));

        BigDecimal period = onTime.add(offTime);

        BigDecimal bigDecimalSampleRate = BigDecimal.valueOf(audioRequest.getSampleRate());
        long remainderInCycle = positionSecond.remainder(period).multiply(bigDecimalSampleRate).setScale(0, RoundingMode.HALF_UP).longValue();
        long onSampleCount = onTime.multiply(bigDecimalSampleRate).setScale(0, RoundingMode.HALF_UP).longValue();
        long offSampleCount = offTime.multiply(bigDecimalSampleRate).setScale(0, RoundingMode.HALF_UP).longValue();

        boolean isOne = false;
        long remainingSamplesInState;
        if (remainderInCycle >= offSampleCount) {
            isOne = true;
            remainingSamplesInState = (onSampleCount + offSampleCount) - remainderInCycle;
        } else {
            isOne = false;
            remainingSamplesInState = offSampleCount - remainderInCycle;
        }

        AudioFrameResult result = createResultBuffer(audioRequest);

        int maxValue = (int) ((1 << ((audioRequest.getBytesPerSample() * 8) - 1)) * amplitude);

        int sampleIndex = 0;
        while (sampleIndex < result.getNumberSamples()) {
            int valueToSet = isOne ? maxValue : -maxValue;
            while (sampleIndex < result.getNumberSamples() && remainingSamplesInState > 0) {
                for (int i = 0; i < audioRequest.getNumberOfChannels(); ++i) {
                    result.setSampleAt(i, sampleIndex, valueToSet);
                }
                ++sampleIndex;
                --remainingSamplesInState;
            }
            isOne = !isOne;
            remainingSamplesInState = isOne ? onSampleCount : offSampleCount;
        }

        return result;
    }

    protected AudioFrameResult createResultBuffer(AudioRequest audioRequest) {
        List<ByteBuffer> channels = new ArrayList<>();
        for (int i = 0; i < audioRequest.getNumberOfChannels(); ++i) {
            int bytesPerChannel = audioRequest.getBytesPerSample() * (audioRequest.getLength().getSeconds().multiply(BigDecimal.valueOf(audioRequest.getSampleRate()))).intValue();
            ByteBuffer buffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(bytesPerChannel);
            channels.add(buffer);
        }
        return new AudioFrameResult(channels, audioRequest.getSampleRate(), audioRequest.getBytesPerSample());
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new ProceduralSquareWaveAudioClip(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();
        onTimeProvider = new DoubleProvider(Math.log10(1.0 / 20000.0), Math.log10(1.0 / 20.0), new ConstantInterpolator(Math.log10(1.0 / 700.0)));
        offTimeProvider = new DoubleProvider(Math.log10(1.0 / 20000.0), Math.log10(1.0 / 20.0), new ConstantInterpolator(Math.log10(1.0 / 700.0)));
        amplitudeProvider = new DoubleProvider(0.0, 1.0, new ConstantInterpolator(1.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();
        ValueProviderDescriptor onTimeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(onTimeProvider)
                .withName("On time (log10)")
                .build();
        ValueProviderDescriptor offTimeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(offTimeProvider)
                .withName("Off time (log10)")
                .build();
        result.add(onTimeDescriptor);
        result.add(offTimeDescriptor);
        return result;
    }

}
