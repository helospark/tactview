package com.helospark.tactview.ui.javafx.inputmode.strategy;

import javafx.scene.canvas.GraphicsContext;

public interface InputTypeStrategy<T> {

    public default void onMouseDownEvent(StrategyMouseInput input) {
    }

    public default void onMouseMovedEvent(StrategyMouseInput input) {
    }

    public default void onMouseUpEvent(StrategyMouseInput input) {
    }

    public default void onMouseDraggedEvent(StrategyMouseInput input) {
    }

    public default void onKeyReleasedEvent(StrategyKeyInput input) {
    }

    public ResultType getResultType();

    public void draw(GraphicsContext canvas, int width, int height);

    public T getResult();

}
