package com.helospark.tactview.ui.javafx.inputmode.strategy;

import javafx.scene.canvas.GraphicsContext;

public interface InputTypeStrategy<T> {

    public default void onMouseDownEvent(StrategyInput input) {
    }

    public default void onMouseMovedEvent(StrategyInput input) {
    }

    public default void onMouseUpEvent(StrategyInput input) {
    }

    public default void onMouseDraggedEvent(StrategyInput input) {
    }

    public ResultType getResultType();

    public void draw(GraphicsContext canvas);

    public T getResult();

}
