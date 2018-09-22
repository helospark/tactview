package com.helospark.tactview.core.decoder;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineLength;

public class MediaMetadata {
    private double fps;
    private int width;
    private int height;
    private TimelineLength lengthInMilliseconds;

    @Generated("SparkTools")
    private MediaMetadata(Builder builder) {
        this.fps = builder.fps;
        this.width = builder.width;
        this.height = builder.height;
        this.lengthInMilliseconds = builder.lengthInMilliseconds;
    }

    public double getFps() {
        return fps;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public TimelineLength getLengthInMilliseconds() {
        return lengthInMilliseconds;
    }

    @Override
    public String toString() {
        return "MediaMetadata [fps=" + fps + ", width=" + width + ", height=" + height + ", lengthInMilliseconds=" + lengthInMilliseconds + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private double fps;
        private int width;
        private int height;
        private TimelineLength lengthInMilliseconds;

        private Builder() {
        }

        public Builder withFps(double fps) {
            this.fps = fps;
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

        public Builder withLengthInMilliseconds(TimelineLength lengthInMilliseconds) {
            this.lengthInMilliseconds = lengthInMilliseconds;
            return this;
        }

        public MediaMetadata build() {
            return new MediaMetadata(this);
        }
    }

}
