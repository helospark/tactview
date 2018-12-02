package com.helospark.tactview.core.timeline.effect.scale.service;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class ScaleRequest {
    private ReadOnlyClipImage image;
    private int newWidth;
    private int newHeight;
    private Boolean padImage;

    @Generated("SparkTools")
    private ScaleRequest(Builder builder) {
        this.image = builder.image;
        this.newWidth = builder.newWidth;
        this.newHeight = builder.newHeight;
        this.padImage = builder.padImage;
    }

    public ReadOnlyClipImage getImage() {
        return image;
    }

    public int getNewWidth() {
        return newWidth;
    }

    public int getNewHeight() {
        return newHeight;
    }

    public boolean isPadImage() {
        return padImage == null ? true : padImage;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ReadOnlyClipImage image;
        private int newWidth;
        private int newHeight;
        private Boolean padImage;

        private Builder() {
        }

        public Builder withImage(ReadOnlyClipImage image) {
            this.image = image;
            return this;
        }

        public Builder withNewWidth(int newWidth) {
            this.newWidth = newWidth;
            return this;
        }

        public Builder withNewHeight(int newHeight) {
            this.newHeight = newHeight;
            return this;
        }

        public Builder withPadImage(boolean padImage) {
            this.padImage = padImage;
            return this;
        }

        public ScaleRequest build() {
            return new ScaleRequest(this);
        }
    }
}
