package com.helospark.tactview.core.decoder;

import java.io.File;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class AudioMediaDataRequest {
    private File file;
    private AudioMediaMetadata metadata;

    private TimelinePosition start;

    @Generated("SparkTools")
    private AudioMediaDataRequest(Builder builder) {
        this.file = builder.file;
        this.metadata = builder.metadata;
        this.start = builder.start;
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

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private File file;
        private AudioMediaMetadata metadata;
        private TimelinePosition start;

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

        public Builder withStart(TimelinePosition start) {
            this.start = start;
            return this;
        }

        public AudioMediaDataRequest build() {
            return new AudioMediaDataRequest(this);
        }
    }

}
