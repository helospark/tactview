package com.helospark.tactview.core.timeline.effect.blur.service;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class LinearBlurRequest {
    InterpolationLine direction;
    ReadOnlyClipImage input;

    @Generated("SparkTools")
    private LinearBlurRequest(Builder builder) {
        this.direction = builder.direction;
        this.input = builder.input;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private InterpolationLine direction;
        private ReadOnlyClipImage input;

        private Builder() {
        }

        public Builder withDirection(InterpolationLine direction) {
            this.direction = direction;
            return this;
        }

        public Builder withInput(ReadOnlyClipImage input) {
            this.input = input;
            return this;
        }

        public LinearBlurRequest build() {
            return new LinearBlurRequest(this);
        }
    }
}
