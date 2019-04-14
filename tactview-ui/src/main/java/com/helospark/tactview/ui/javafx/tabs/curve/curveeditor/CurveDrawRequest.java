package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class CurveDrawRequest extends AbstractCurveEditorRequest {
    public GraphicsContext graphics;

    @Generated("SparkTools")
    private CurveDrawRequest(Builder builder) {
        this.currentProvider = builder.currentProvider;
        this.currentKeyframeableEffect = builder.currentKeyframeableEffect;
        this.curveViewerOffsetSeconds = builder.curveViewerOffsetSeconds;
        this.secondsPerPixel = builder.secondsPerPixel;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.displayScale = builder.displayScale;
        this.height = builder.height;
        this.canvas = builder.canvas;
        this.graphics = builder.graphics;
    }

    public CurveDrawRequest(Canvas canvas, GraphicsContext graphics) {
        this.canvas = canvas;
        this.graphics = graphics;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private KeyframeableEffect currentProvider;
        private DoubleInterpolator currentKeyframeableEffect;
        private double curveViewerOffsetSeconds;
        private double secondsPerPixel;
        private double minValue;
        private double maxValue;
        private double displayScale;
        private double height;
        private Canvas canvas;
        private GraphicsContext graphics;

        private Builder() {
        }

        public Builder withCurrentProvider(KeyframeableEffect currentProvider) {
            this.currentProvider = currentProvider;
            return this;
        }

        public Builder withCurrentKeyframeableEffect(DoubleInterpolator currentKeyframeableEffect) {
            this.currentKeyframeableEffect = currentKeyframeableEffect;
            return this;
        }

        public Builder withCurveViewerOffsetSeconds(double curveViewerOffsetSeconds) {
            this.curveViewerOffsetSeconds = curveViewerOffsetSeconds;
            return this;
        }

        public Builder withSecondsPerPixel(double secondsPerPixel) {
            this.secondsPerPixel = secondsPerPixel;
            return this;
        }

        public Builder withMinValue(double minValue) {
            this.minValue = minValue;
            return this;
        }

        public Builder withMaxValue(double maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public Builder withDisplayScale(double displayScale) {
            this.displayScale = displayScale;
            return this;
        }

        public Builder withHeight(double height) {
            this.height = height;
            return this;
        }

        public Builder withCanvas(Canvas canvas) {
            this.canvas = canvas;
            return this;
        }

        public Builder withGraphics(GraphicsContext graphics) {
            this.graphics = graphics;
            return this;
        }

        public CurveDrawRequest build() {
            return new CurveDrawRequest(this);
        }
    }

}
