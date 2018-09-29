package com.helospark.tactview.core.decoder;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineLength;

public class ImageMetadata extends VisualMediaMetadata {

    @Generated("SparkTools")
    private ImageMetadata(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.length = builder.length;
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

        public ImageMetadata build() {
            return new ImageMetadata(this);
        }
    }

}
