package com.helospark.tactview.core.decoder;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineLength;

public class AudioMediaMetadata extends MediaMetadata {
    protected int sampleRate;
    protected int bytesPerSample;
    protected int channels;

    @Generated("SparkTools")
    private AudioMediaMetadata(Builder builder) {
        this.length = builder.length;
        this.sampleRate = builder.sampleRate;
        this.bytesPerSample = builder.bytesPerSample;
        this.channels = builder.channels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getChannels() {
        return channels;
    }

    public int getBytesPerSample() {
        return bytesPerSample;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineLength length;
        private int sampleRate;
        private int bytesPerSample;
        private int channels;

        private Builder() {
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

        public Builder withChannels(int channels) {
            this.channels = channels;
            return this;
        }

        public AudioMediaMetadata build() {
            return new AudioMediaMetadata(this);
        }
    }

}