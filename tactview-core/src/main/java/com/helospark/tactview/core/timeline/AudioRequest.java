package com.helospark.tactview.core.timeline;

import javax.annotation.Generated;

public class AudioRequest {
    private TimelinePosition position;
    private TimelineLength length;
    private int sampleRate;
    private int bytesPerSample;
    private boolean applyEffects;

    @Generated("SparkTools")
    private AudioRequest(Builder builder) {
        this.position = builder.position;
        this.length = builder.length;
        this.sampleRate = builder.sampleRate;
        this.bytesPerSample = builder.bytesPerSample;
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

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelinePosition position;
        private TimelineLength length;
        private int sampleRate;
        private int bytesPerSample;
        private boolean applyEffects;

        private Builder() {
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

        public Builder withApplyEffects(boolean applyEffects) {
            this.applyEffects = applyEffects;
            return this;
        }

        public AudioRequest build() {
            return new AudioRequest(this);
        }
    }

}
