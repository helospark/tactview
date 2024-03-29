package com.helospark.tactview.core.timeline;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class AudioRequest {
    private EvaluationContext evaluationContext;
    private TimelinePosition position;
    private TimelineLength length;
    private int sampleRate;
    private int bytesPerSample;
    private int numberOfChannels;
    private boolean applyEffects;
    private Map<String, ReadOnlyClipImage> requestedVideoClips;
    private Map<String, AudioFrameResult> requestedAudioClips;

    @Generated("SparkTools")
    private AudioRequest(Builder builder) {
        this.evaluationContext = builder.evaluationContext;
        this.position = builder.position;
        this.length = builder.length;
        this.sampleRate = builder.sampleRate;
        this.bytesPerSample = builder.bytesPerSample;
        this.numberOfChannels = builder.numberOfChannels;
        this.applyEffects = builder.applyEffects;
        this.requestedVideoClips = builder.requestedVideoClips;
        this.requestedAudioClips = builder.requestedAudioClips;
    }

    public TimelineLength getLength() {
        return length;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public boolean isApplyEffects() {
        return applyEffects;
    }

    public int getBytesPerSample() {
        return bytesPerSample;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    public Map<String, ReadOnlyClipImage> getRequestedVideoClips() {
        return requestedVideoClips;
    }

    public Map<String, AudioFrameResult> getRequestedAudioClips() {
        return requestedAudioClips;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static Builder builderFrom(AudioRequest audioRequest) {
        return new Builder(audioRequest);
    }

    @Generated("SparkTools")
    public static final class Builder {
        private EvaluationContext evaluationContext;
        private TimelinePosition position;
        private TimelineLength length;
        private int sampleRate;
        private int bytesPerSample;
        private int numberOfChannels;
        private boolean applyEffects;
        private Map<String, ReadOnlyClipImage> requestedVideoClips = Collections.emptyMap();
        private Map<String, AudioFrameResult> requestedAudioClips = Collections.emptyMap();

        private Builder() {
        }

        private Builder(AudioRequest audioRequest) {
            this.evaluationContext = audioRequest.evaluationContext;
            this.position = audioRequest.position;
            this.length = audioRequest.length;
            this.sampleRate = audioRequest.sampleRate;
            this.bytesPerSample = audioRequest.bytesPerSample;
            this.numberOfChannels = audioRequest.numberOfChannels;
            this.applyEffects = audioRequest.applyEffects;
            this.requestedVideoClips = audioRequest.requestedVideoClips;
            this.requestedAudioClips = audioRequest.requestedAudioClips;
        }

        public Builder withEvaluationContext(EvaluationContext evaluationContext) {
            this.evaluationContext = evaluationContext;
            return this;
        }

        public Builder withPosition(TimelinePosition position) {
            this.position = position;
            return this;
        }

        public Builder withLength(TimelineLength length) {
            this.length = length;
            return this;
        }

        public Builder withSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        public Builder withBytesPerSample(int bytesPerSample) {
            this.bytesPerSample = bytesPerSample;
            return this;
        }

        public Builder withNumberOfChannels(int numberOfChannels) {
            this.numberOfChannels = numberOfChannels;
            return this;
        }

        public Builder withApplyEffects(boolean applyEffects) {
            this.applyEffects = applyEffects;
            return this;
        }

        public Builder withRequestedVideoClips(Map<String, ReadOnlyClipImage> requestedVideoClips) {
            this.requestedVideoClips = requestedVideoClips;
            return this;
        }

        public Builder withRequestedAudioClips(Map<String, AudioFrameResult> requestedAudioClips) {
            this.requestedAudioClips = requestedAudioClips;
            return this;
        }

        public AudioRequest build() {
            return new AudioRequest(this);
        }
    }

}
