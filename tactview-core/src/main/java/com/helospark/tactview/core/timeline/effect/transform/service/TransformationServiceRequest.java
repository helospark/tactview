package com.helospark.tactview.core.timeline.effect.transform.service;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class TransformationServiceRequest {
    private ReadOnlyClipImage image;
    private float[][] convolutionMatrix;
    private boolean flipRedAndBlue;

    @Generated("SparkTools")
    private TransformationServiceRequest(Builder builder) {
        this.image = builder.image;
        this.convolutionMatrix = builder.convolutionMatrix;
        this.flipRedAndBlue = builder.flipRedAndBlue;
    }

    public ReadOnlyClipImage getImage() {
        return image;
    }

    public float[][] getConvolutionMatrix() {
        return convolutionMatrix;
    }

    public boolean isFlipRedAndBlue() {
        return flipRedAndBlue;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ReadOnlyClipImage image;
        private float[][] convolutionMatrix;
        private boolean flipRedAndBlue;

        private Builder() {
        }

        public Builder withImage(ReadOnlyClipImage image) {
            this.image = image;
            return this;
        }

        public Builder withConvolutionMatrix(float[][] convolutionMatrix) {
            this.convolutionMatrix = convolutionMatrix;
            return this;
        }

        public Builder withFlipRedAndBlue(boolean flipRedAndBlue) {
            this.flipRedAndBlue = flipRedAndBlue;
            return this;
        }

        public TransformationServiceRequest build() {
            return new TransformationServiceRequest(this);
        }
    }
}
