package com.helospark.tactview.core.timeline.effect.layermask;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class LayerMaskBetweenTwoImageApplyRequest {
    private ReadOnlyClipImage topFrame;
    private ReadOnlyClipImage bottomFrame;
    private ReadOnlyClipImage mask;
    private LayerMaskAlphaCalculator calculator;

    @Generated("SparkTools")
    private LayerMaskBetweenTwoImageApplyRequest(Builder builder) {
        this.topFrame = builder.topFrame;
        this.bottomFrame = builder.bottomFrame;
        this.mask = builder.mask;
        this.calculator = builder.calculator;
    }

    public ReadOnlyClipImage getTopFrame() {
        return topFrame;
    }

    public ReadOnlyClipImage getBottomFrame() {
        return bottomFrame;
    }

    public ReadOnlyClipImage getMask() {
        return mask;
    }

    public LayerMaskAlphaCalculator getCalculator() {
        return calculator;
    }

    @Override
    public String toString() {
        return "LayerMaskBetweenTwoImageApplyRequest [topFrame=" + topFrame + ", bottomFrame=" + bottomFrame + ", mask=" + mask + ", calculator=" + calculator + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private ReadOnlyClipImage topFrame;
        private ReadOnlyClipImage bottomFrame;
        private ReadOnlyClipImage mask;
        private LayerMaskAlphaCalculator calculator;

        private Builder() {
        }

        public Builder withTopFrame(ReadOnlyClipImage topFrame) {
            this.topFrame = topFrame;
            return this;
        }

        public Builder withBottomFrame(ReadOnlyClipImage bottomFrame) {
            this.bottomFrame = bottomFrame;
            return this;
        }

        public Builder withMask(ReadOnlyClipImage mask) {
            this.mask = mask;
            return this;
        }

        public Builder withCalculator(LayerMaskAlphaCalculator calculator) {
            this.calculator = calculator;
            return this;
        }

        public LayerMaskBetweenTwoImageApplyRequest build() {
            return new LayerMaskBetweenTwoImageApplyRequest(this);
        }
    }

}
