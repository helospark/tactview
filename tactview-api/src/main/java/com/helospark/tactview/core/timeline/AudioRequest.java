package com.helospark.tactview.core.timeline;

public class AudioRequest {
    private TimelinePosition position;
    private TimelineLength length;
    private int sampleRate;
    private int bytesPerSample;
    private int numberOfChannels;
    private boolean applyEffects;

    private AudioRequest(Builder builder) {
        this.position = builder.position;
        this.length = builder.length;
        this.sampleRate = builder.sampleRate;
        this.bytesPerSample = builder.bytesPerSample;
        this.numberOfChannels = builder.numberOfChannels;
        this.applyEffects = builder.applyEffects;
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

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builderFrom(AudioRequest audioRequest) {
        return new Builder(audioRequest);
    }

    public static final class Builder {
        private TimelinePosition position;
        private TimelineLength length;
        private int sampleRate;
        private int bytesPerSample;
        private int numberOfChannels;
        private boolean applyEffects;
        private Builder() {
        }

        private Builder(AudioRequest audioRequest) {
            this.position = audioRequest.position;
            this.length = audioRequest.length;
            this.sampleRate = audioRequest.sampleRate;
            this.bytesPerSample = audioRequest.bytesPerSample;
            this.numberOfChannels = audioRequest.numberOfChannels;
            this.applyEffects = audioRequest.applyEffects;
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

        public AudioRequest build() {
            return new AudioRequest(this);
        }
    }

}
