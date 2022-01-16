package com.helospark.tactview.ui.javafx.inputmode.strategy;

import java.util.Optional;

import javax.annotation.Generated;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

public class DrawRequestParameter {
    private GraphicsContext canvas;
    private int width;
    private int height;
    private boolean mouseInCanvas;
    private Optional<MouseEvent> mouseEvent;

    private double canvasTranslateX;
    private double canvasTranslateY;

    @Generated("SparkTools")
    private DrawRequestParameter(Builder builder) {
        this.canvas = builder.canvas;
        this.width = builder.width;
        this.height = builder.height;
        this.mouseInCanvas = builder.mouseInCanvas;
        this.mouseEvent = builder.mouseEvent;
        this.canvasTranslateX = builder.canvasTranslateX;
        this.canvasTranslateY = builder.canvasTranslateY;
    }

    public GraphicsContext getCanvas() {
        return canvas;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isMouseInCanvas() {
        return mouseInCanvas;
    }

    public Optional<MouseEvent> getMouseEvent() {
        return mouseEvent;
    }

    public double getCanvasTranslateX() {
        return canvasTranslateX;
    }

    public double getCanvasTranslateY() {
        return canvasTranslateY;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private GraphicsContext canvas;
        private int width;
        private int height;
        private boolean mouseInCanvas;
        private Optional<MouseEvent> mouseEvent = Optional.empty();
        private double canvasTranslateX;
        private double canvasTranslateY;

        private Builder() {
        }

        public Builder withCanvas(GraphicsContext canvas) {
            this.canvas = canvas;
            return this;
        }

        public Builder withWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder withMouseInCanvas(boolean mouseInCanvas) {
            this.mouseInCanvas = mouseInCanvas;
            return this;
        }

        public Builder withMouseEvent(Optional<MouseEvent> mouseEvent) {
            this.mouseEvent = mouseEvent;
            return this;
        }

        public Builder withCanvasTranslateX(double canvasTranslateX) {
            this.canvasTranslateX = canvasTranslateX;
            return this;
        }

        public Builder withCanvasTranslateY(double canvasTranslateY) {
            this.canvasTranslateY = canvasTranslateY;
            return this;
        }

        public DrawRequestParameter build() {
            return new DrawRequestParameter(this);
        }
    }
}