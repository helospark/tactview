package com.helospark.tactview.core.decoder.gif;

import javax.annotation.Generated;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.TimelineLength;

public class GifVideoMetadata extends VisualMediaMetadata {
    private int numberOfFrames;
    private int loopCount;

    @Generated("SparkTools")
    private GifVideoMetadata(Builder builder) {
        this.length = builder.length;
        this.width = builder.width;
        this.height = builder.height;
        this.resizable = builder.resizable;
        this.numberOfFrames = builder.numberOfFrames;
        this.loopCount = builder.loopCount;
    }

    public long getNumberOfFrames() {
        return numberOfFrames;
    }

    public int getLoopCount() {
        return loopCount;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private TimelineLength length;
        private int width;
        private int height;
        private boolean resizable;
        private int numberOfFrames;
        private int loopCount;

        private Builder() {
        }

        public Builder withLength(TimelineLength length) {
            this.length = length;
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

        public Builder withResizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        public Builder withNumberOfFrames(int numberOfFrames) {
            this.numberOfFrames = numberOfFrames;
            return this;
        }

        public Builder withLoopCount(int loopCount) {
            this.loopCount = loopCount;
            return this;
        }

        public GifVideoMetadata build() {
            return new GifVideoMetadata(this);
        }
    }

}
