package com.helospark.tactview.core.timeline.effect.blur;

import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.ClipFrameResult;

public class BlurRequest {
    private ClipFrameResult image;
    private int kernelWidth;
    private int kernelHeight;
    private Optional<Region> region;

    @Generated("SparkTools")
    private BlurRequest(Builder builder) {
        this.image = builder.image;
        this.kernelWidth = builder.kernelWidth;
        this.kernelHeight = builder.kernelHeight;
        this.region = builder.region;
    }

    public ClipFrameResult getImage() {
        return image;
    }

    public int getKernelWidth() {
        return kernelWidth;
    }

    public int getKernelHeight() {
        return kernelHeight;
    }

    public Optional<Region> getRegion() {
        return region;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ClipFrameResult image;
        private int kernelWidth;
        private int kernelHeight;
        private Optional<Region> region = Optional.empty();

        private Builder() {
        }

        public Builder withImage(ClipFrameResult image) {
            this.image = image;
            return this;
        }

        public Builder withKernelWidth(int kernelWidth) {
            this.kernelWidth = kernelWidth;
            return this;
        }

        public Builder withKernelHeight(int kernelHeight) {
            this.kernelHeight = kernelHeight;
            return this;
        }

        public Builder withRegion(Optional<Region> region) {
            this.region = region;
            return this;
        }

        public BlurRequest build() {
            return new BlurRequest(this);
        }
    }

}
