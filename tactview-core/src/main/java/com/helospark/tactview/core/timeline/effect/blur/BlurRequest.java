package com.helospark.tactview.core.timeline.effect.blur;

import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class BlurRequest {
    private ReadOnlyClipImage image;
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

    public ReadOnlyClipImage getImage() {
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
        private ReadOnlyClipImage image;
        private int kernelWidth;
        private int kernelHeight;
        private Optional<Region> region = Optional.empty();

        private Builder() {
        }

        public Builder withImage(ReadOnlyClipImage image) {
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
