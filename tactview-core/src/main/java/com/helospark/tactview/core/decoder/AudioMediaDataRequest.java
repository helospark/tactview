package com.helospark.tactview.core.decoder;

import java.io.File;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class AudioMediaDataRequest {
    private File file;
    private AudioMediaMetadata metadata;
    private int expectedSampleRate;
    private int expectedBytesPerSample;
    private int expectedChannels;

    private TimelinePosition start;
    private TimelineLength length;

    @Generated("SparkTools")
    private AudioMediaDataRequest(Builder builder) {
        this.file = builder.file;
        this.metadata = builder.metadata;
        this.expectedSampleRate = builder.expectedSampleRate;
        this.expectedBytesPerSample = builder.expectedBytesPerSample;
        this.expectedChannels = builder.expectedChannels;
        this.start = builder.start;
        this.length = builder.length;
    }

    @Generated("SparkTools")
    public AudioMediaDataRequest() {
    }

    public File getFile() {
        return file;
    }

    public TimelinePosition getStart() {
        return start;
    }

    public AudioMediaMetadata getMetadata() {
        return metadata;
    }

    public int getExpectedSampleRate() {
        return expectedSampleRate;
    }

    public int getExpectedBytesPerSample() {
        return expectedBytesPerSample;
    }

    public int getExpectedChannels() {
        return expectedChannels;
    }

    public TimelineLength getLength() {
        return length;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private File file;
        private AudioMediaMetadata metadata;
        private int expectedSampleRate;
        private int expectedBytesPerSample;
        private int expectedChannels;
        private TimelinePosition start;
        private TimelineLength length;

        private Builder() {
        }

        public Builder withFile(File file) {
            this.file = file;
            return this;
        }

        public Builder withMetadata(AudioMediaMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder withExpectedSampleRate(int expectedSampleRate) {
            this.expectedSampleRate = expectedSampleRate;
            return this;
        }

        public Builder withExpectedBytesPerSample(int expectedBytesPerSample) {
            this.expectedBytesPerSample = expectedBytesPerSample;
            return this;
        }

        public Builder withExpectedChannels(int expectedChannels) {
            this.expectedChannels = expectedChannels;
            return this;
        }

        public Builder withStart(TimelinePosition start) {
            this.start = start;
            return this;
        }

        public Builder withLength(TimelineLength length) {
            this.length = length;
            return this;
        }

        public AudioMediaDataRequest build() {
            return new AudioMediaDataRequest(this);
        }
    }

}
