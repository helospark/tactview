package com.helospark.tactview.ui.javafx.inputmode.strategy;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

public interface InputTypeStrategy<T> {

    public default void onMouseDownEvent(int x, int y, MouseEvent mouseEvent) {
    }

    public default void onMouseMovedEvent(int x, int y, MouseEvent mouseEvent) {
    }

    public default void onMouseUpEvent(int x, int y, MouseEvent mouseEvent) {
    }

    public ResultType getResultType();

    public void draw(GraphicsContext canvas);

    public T getResult();

    public default void onMouseDraggedEvent(int x, int y, MouseEvent e) {
    }

}
