package com.helospark.tactview.core.decoder;

import java.math.BigDecimal;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineLength;

public class VideoMetadata extends VisualMediaMetadata {
    protected double fps;

    @Generated("SparkTools")
    private VideoMetadata(Builder builder) {
        this.length = builder.length;
        this.width = builder.width;
        this.height = builder.height;
        this.resizable = builder.resizable;
        this.fps = builder.fps;
    }

    protected VideoMetadata() {

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

    public boolean isValid() {
        return width > 0 && height > 0 && fps > 0;
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
        private double fps;

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

        public Builder withFps(double fps) {
            this.fps = fps;
            return this;
        }

        public VideoMetadata build() {
            return new VideoMetadata(this);
        }
    }

}
