package com.helospark.tactview.ui.javafx.inputmode.strategy.generalops;

import java.util.Collections;
import java.util.Map;

import com.helospark.tactview.core.timeline.TimelineRenderResult.RegularRectangle;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class GeneralCanvasOperationsMouseRequest {
    double x;
    double y;
    double unscaledX;
    double unscaledY;
    MouseEvent mouseEvent;
    Map<String, RegularRectangle> rectangles;
    Canvas canvas;

    private GeneralCanvasOperationsMouseRequest(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.unscaledX = builder.unscaledX;
        this.unscaledY = builder.unscaledY;
        this.mouseEvent = builder.mouseEvent;
        this.rectangles = builder.rectangles;
        this.canvas = builder.canvas;
    }

    public static Builder builder() {
        return new Builder();
    }
    public static final class Builder {
        private double x;
        private double y;
        private double unscaledX;
        private double unscaledY;
        private MouseEvent mouseEvent;
        private Map<String, RegularRectangle> rectangles = Collections.emptyMap();
        private Canvas canvas;
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

        public GeneralCanvasOperationsMouseRequest build() {
            return new GeneralCanvasOperationsMouseRequest(this);
        }
    }

}
