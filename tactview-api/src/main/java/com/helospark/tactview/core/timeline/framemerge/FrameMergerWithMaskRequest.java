package com.helospark.tactview.core.timeline.framemerge;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class FrameMergerWithMaskRequest {
    ReadOnlyClipImage top;
    ReadOnlyClipImage bottom;
    ReadOnlyClipImage mask;
    boolean scale;
    boolean invert;

    @Generated("SparkTools")
    private FrameMergerWithMaskRequest(Builder builder) {
        this.top = builder.top;
        this.bottom = builder.bottom;
        this.mask = builder.mask;
        this.scale = builder.scale;
        this.invert = builder.invert;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ReadOnlyClipImage top;
        private ReadOnlyClipImage bottom;
        private ReadOnlyClipImage mask;
        private boolean scale;
        private boolean invert;

        private Builder() {
        }

        public Builder withTop(ReadOnlyClipImage top) {
            this.top = top;
            return this;
        }

        public Builder withBottom(ReadOnlyClipImage bottom) {
            this.bottom = bottom;
            return this;
        }

        public Builder withMask(ReadOnlyClipImage mask) {
            this.mask = mask;
            return this;
        }

        public Builder withScale(boolean scale) {
            this.scale = scale;
            return this;
        }

        public Builder withInvert(boolean invert) {
            this.invert = invert;
            return this;
        }

        public FrameMergerWithMaskRequest build() {
            return new FrameMergerWithMaskRequest(this);
        }
    }
}
