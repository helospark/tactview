package com.helospark.tactview.ui.javafx.inputmode.strategy;

import javax.annotation.Generated;

import javafx.scene.input.MouseEvent;

public class StrategyInput {
    public double x;
    public double y;
    public double unscaledX;
    public double unscaledY;
    public MouseEvent mouseEvent;

    @Generated("SparkTools")
    private StrategyInput(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.unscaledX = builder.unscaledX;
        this.unscaledY = builder.unscaledY;
        this.mouseEvent = builder.mouseEvent;
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

        public StrategyInput build() {
            return new StrategyInput(this);
        }
    }
}
