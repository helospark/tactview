package com.helospark.tactview.core.decoder;

import java.math.BigDecimal;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineLength;

public class VideoMetadata extends VisualMediaMetadata {
    protected double fps;
    protected int bitRate;
    protected double rotation;
    protected boolean hwDecodingSupported; // Not ideal that this leaks here

    @Generated("SparkTools")
    private VideoMetadata(Builder builder) {
        this.length = builder.length;
        this.width = builder.width;
        this.height = builder.height;
        this.resizable = builder.resizable;
        this.fps = builder.fps;
        this.bitRate = builder.bitRate;
        this.rotation = builder.rotation;
        this.hwDecodingSupported = builder.hwDecodingSupported;
    }

    protected VideoMetadata() {

    }

    public int getBitRate() {
        return bitRate;
    }

    public double getFps() {
        return fps;
    }

    public double getRotation() {
        return rotation;
    }

    public boolean isHwDecodingSupported() {
        return hwDecodingSupported;
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

    @Override
    public String toString() {
        return "VideoMetadata [fps=" + fps + ", bitRate=" + bitRate + ", width=" + width + ", height=" + height + ", resizable=" + resizable + ", length=" + length + ", rotation=" + rotation
                + ", hwDecodingSupported=" + hwDecodingSupported + "]";
    }

    /**
     * Creates builder to build {@link VideoMetadata}.
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link VideoMetadata}.
     */
    @Generated("SparkTools")
    public static final class Builder {
        private TimelineLength length;
        private int width;
        private int height;
        private boolean resizable;
        private double fps;
        private int bitRate;
        private double rotation;
        private boolean hwDecodingSupported;

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

        public Builder withBitRate(int bitRate) {
            this.bitRate = bitRate;
            return this;
        }

        public Builder withRotation(double rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder withHwDecodingSupported(boolean hwDecodingSupported) {
            this.hwDecodingSupported = hwDecodingSupported;
            return this;
        }

        public VideoMetadata build() {
            return new VideoMetadata(this);
        }
    }

}
