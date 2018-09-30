package com.helospark.tactview.core.decoder;

import java.math.BigDecimal;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineLength;

public class VideoMetadata extends VisualMediaMetadata {
    private double fps;

    @Generated("SparkTools")
    private VideoMetadata(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.length = builder.length;
        this.fps = builder.fps;
    }

    public double getFps() {
        return fps;
    }

    @Override
    public TimelineLength getLength() {
        return length;
    }

    public long getNumberOfFrames() {
        return length.getSeconds().multiply(BigDecimal.valueOf(fps)).longValue();
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private int width;
        private int height;
        private TimelineLength length;
        private double fps;

        private Builder() {
        }

        public Builder withWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder withLength(TimelineLength length) {
            this.length = length;
            return this;
        }

        public Builder withFps(double fps) {
            this.fps = fps;
            return this;
        }

        public VideoMetadata build() {
            return new VideoMetadata(this);
        }
    }

}
