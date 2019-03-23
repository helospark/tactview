package com.helospark.tactview.ui.javafx.inputmode.strategy;

import java.util.function.Supplier;

import javax.annotation.Generated;

import com.helospark.tactview.ui.javafx.key.CurrentlyPressedKeyRepository;

import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

public class StrategyMouseInput {
    public double x;
    public double y;
    public double unscaledX;
    public double unscaledY;
    public MouseEvent mouseEvent;
    public Supplier<Image> canvasImage;
    public CurrentlyPressedKeyRepository currentlyPressedKeyRepository;

    @Generated("SparkTools")
    private StrategyMouseInput(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.unscaledX = builder.unscaledX;
        this.unscaledY = builder.unscaledY;
        this.mouseEvent = builder.mouseEvent;
        this.canvasImage = builder.canvasImage;
        this.currentlyPressedKeyRepository = builder.currentlyPressedKeyRepository;
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
        private MouseEvent mouseEvent;
        private Supplier<Image> canvasImage;
        private CurrentlyPressedKeyRepository currentlyPressedKeyRepository;

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

        public Builder withCanvasImage(Supplier<Image> canvasImage) {
            this.canvasImage = canvasImage;
            return this;
        }

        public Builder withCurrentlyPressedKeyRepository(CurrentlyPressedKeyRepository currentlyPressedKeyRepository) {
            this.currentlyPressedKeyRepository = currentlyPressedKeyRepository;
            return this;
        }

        public StrategyMouseInput build() {
            return new StrategyMouseInput(this);
        }
    }
}
