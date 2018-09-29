package com.helospark.tactview.core.decoder;

import java.io.File;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class MediaDataRequest {
    private File file;
    private VisualMediaMetadata metadata;

    private TimelinePosition start;
    private TimelineLength length;
    private int numberOfFrames;

    private boolean shouldRescale;
    private int width;
    private int height;

    @Generated("SparkTools")
    private MediaDataRequest(Builder builder) {
        this.file = builder.file;
        this.metadata = builder.metadata;
        this.start = builder.start;
        this.length = builder.length;
        this.numberOfFrames = builder.numberOfFrames;
        this.shouldRescale = builder.shouldRescale;
        this.width = builder.width;
        this.height = builder.height;
    }

    @Generated("SparkTools")
    public MediaDataRequest() {
    }

    public File getFile() {
        return file;
    }

    public VisualMediaMetadata getMetadata() {
        return metadata;
    }

    public TimelinePosition getStart() {
        return start;
    }

    public TimelineLength getLength() {
        return length;
    }

    public boolean isShouldRescale() {
        return shouldRescale;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private File file;
        private VisualMediaMetadata metadata;
        private TimelinePosition start;
        private TimelineLength length;
        private int numberOfFrames;
        private boolean shouldRescale;
        private int width;
        private int height;

        private Builder() {
        }

        public Builder withFile(File file) {
            this.file = file;
            return this;
        }

        public Builder withMetadata(VisualMediaMetadata metadata) {
            this.metadata = metadata;
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

        public Builder withNumberOfFrames(int numberOfFrames) {
            this.numberOfFrames = numberOfFrames;
            return this;
        }

        public Builder withShouldRescale(boolean shouldRescale) {
            this.shouldRescale = shouldRescale;
            return this;
        }

        public Builder withWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }

        public MediaDataRequest build() {
            return new MediaDataRequest(this);
        }
    }

}
