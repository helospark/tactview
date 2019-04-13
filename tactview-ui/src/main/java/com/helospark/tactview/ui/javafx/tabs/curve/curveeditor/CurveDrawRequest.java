package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class CurveDrawRequest extends AbstractCurveEditorRequest {
    public Canvas canvas;
    public GraphicsContext graphics;

    @Generated("SparkTools")
    private CurveDrawRequest(Builder builder) {
        this.currentKeyframeableEffect = builder.currentKeyframeableEffect;
        this.canvas = builder.canvas;
        this.graphics = builder.graphics;
        this.curveViewerOffsetSeconds = builder.curveViewerOffsetSeconds;
        this.secondsPerPixel = builder.secondsPerPixel;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.displayScale = builder.displayScale;
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
        private DoubleInterpolator currentKeyframeableEffect;
        private Canvas canvas;
        private GraphicsContext graphics;
        private double curveViewerOffsetSeconds;
        private double secondsPerPixel;
        private double minValue;
        private double maxValue;
        private double displayScale;

        private Builder() {
        }

        public Builder withCurrentKeyframeableEffect(DoubleInterpolator currentKeyframeableEffect) {
            this.currentKeyframeableEffect = currentKeyframeableEffect;
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

        public CurveDrawRequest build() {
            return new CurveDrawRequest(this);
        }
    }

}
