package com.helospark.tactview.ui.javafx.inputmode.strategy.generalops;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelineRenderResult.RegularRectangle;
import com.helospark.tactview.ui.javafx.CanvasStateHolder;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class GeneralCanvasOperationsMouseRequest {
    double x;
    double y;
    double unscaledX;
    double unscaledY;
    double canvasRelativeX;
    double canvasRelativeY;
    MouseEvent mouseEvent;
    Map<String, RegularRectangle> rectangles;
    Canvas canvas;
    CanvasStateHolder canvasStateHolder;

    @Generated("SparkTools")
    private GeneralCanvasOperationsMouseRequest(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.unscaledX = builder.unscaledX;
        this.unscaledY = builder.unscaledY;
        this.canvasRelativeX = builder.canvasRelativeX;
        this.canvasRelativeY = builder.canvasRelativeY;
        this.mouseEvent = builder.mouseEvent;
        this.rectangles = builder.rectangles;
        this.canvas = builder.canvas;
        this.canvasStateHolder = builder.canvasStateHolder;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private double x;
        private double y;
        private double unscaledX;
        private double unscaledY;
        private double canvasRelativeX;
        private double canvasRelativeY;
        private MouseEvent mouseEvent;
        private Map<String, RegularRectangle> rectangles = Collections.emptyMap();
        private Canvas canvas;
        private CanvasStateHolder canvasStateHolder;

        private Builder() {
        }

        public Builder withx(double x) {
            this.x = x;
            return this;
        }

        public Builder withy(double y) {
            this.y = y;
            return this;
        }

        public Builder withUnscaledX(double unscaledX) {
            this.unscaledX = unscaledX;
            return this;
        }

        public Builder withUnscaledY(double unscaledY) {
            this.unscaledY = unscaledY;
            return this;
        }

        public Builder withCanvasRelativeX(double canvasRelativeX) {
            this.canvasRelativeX = canvasRelativeX;
            return this;
        }

        public Builder withCanvasRelativeY(double canvasRelativeY) {
            this.canvasRelativeY = canvasRelativeY;
            return this;
        }

        public Builder withMouseEvent(MouseEvent mouseEvent) {
            this.mouseEvent = mouseEvent;
            return this;
        }

        public Builder withRectangles(Map<String, RegularRectangle> rectangles) {
            this.rectangles = rectangles;
            return this;
        }

        public Builder withCanvas(Canvas canvas) {
            this.canvas = canvas;
            return this;
        }

        public Builder withCanvasStateHolder(CanvasStateHolder canvasStateHolder) {
            this.canvasStateHolder = canvasStateHolder;
            return this;
        }

        public GeneralCanvasOperationsMouseRequest build() {
            return new GeneralCanvasOperationsMouseRequest(this);
        }
    }

}
