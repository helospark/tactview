package com.helospark.tactview.core.decoder;

import java.math.BigDecimal;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineLength;

public class MediaMetadata {
    private double fps;
    private int width;
    private int height;
    private TimelineLength length;

    @Generated("SparkTools")
    private MediaMetadata(Builder builder) {
        this.fps = builder.fps;
        this.width = builder.width;
        this.height = builder.height;
        this.length = builder.length;
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

    public TimelineLength getLength() {
        return length;
    }

    public long getNumberOfFrames() {
        return length.getSeconds().multiply(BigDecimal.valueOf(fps)).longValue();
    }

    @Override
    public String toString() {
        return "MediaMetadata [fps=" + fps + ", width=" + width + ", height=" + height + ", lengthInMilliseconds=" + length + "]";
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
        private TimelineLength length;

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

        public Builder withLength(TimelineLength lengthInMilliseconds) {
            this.length = lengthInMilliseconds;
            return this;
        }

        public MediaMetadata build() {
            return new MediaMetadata(this);
        }
    }

}
