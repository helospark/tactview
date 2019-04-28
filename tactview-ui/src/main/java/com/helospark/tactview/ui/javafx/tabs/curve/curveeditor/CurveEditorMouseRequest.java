package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class CurveEditorMouseRequest extends AbstractCurveEditorRequest {
    public MouseEvent event;

    public Point remappedMousePosition;
    public Point screenMousePosition;
    public Point mouseDelta;

    @Generated("SparkTools")
    private CurveEditorMouseRequest(Builder builder) {
        this.currentProvider = builder.currentProvider;
        this.currentKeyframeableEffect = builder.currentKeyframeableEffect;
        this.curveViewerOffsetSeconds = builder.curveViewerOffsetSeconds;
        this.secondsPerPixel = builder.secondsPerPixel;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.displayScale = builder.displayScale;
        this.height = builder.height;
        this.canvas = builder.canvas;
        this.timeOffset = builder.timeOffset;
        this.event = builder.event;
        this.remappedMousePosition = builder.remappedMousePosition;
        this.screenMousePosition = builder.screenMousePosition;
        this.mouseDelta = builder.mouseDelta;
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
        private double timeOffset;
        private MouseEvent event;
        private Point remappedMousePosition;
        private Point screenMousePosition;
        private Point mouseDelta;

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

        public Builder withTimeOffset(double timeOffset) {
            this.timeOffset = timeOffset;
            return this;
        }

        public Builder withEvent(MouseEvent event) {
            this.event = event;
            return this;
        }

        public Builder withRemappedMousePosition(Point remappedMousePosition) {
            this.remappedMousePosition = remappedMousePosition;
            return this;
        }

        public Builder withScreenMousePosition(Point screenMousePosition) {
            this.screenMousePosition = screenMousePosition;
            return this;
        }

        public Builder withMouseDelta(Point mouseDelta) {
            this.mouseDelta = mouseDelta;
            return this;
        }

        public CurveEditorMouseRequest build() {
            return new CurveEditorMouseRequest(this);
        }
    }

}
