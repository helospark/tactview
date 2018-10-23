package com.helospark.tactview.ui.javafx.inputmode.strategy;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

public interface InputTypeStrategy<T> {

    public default void onMouseDownEvent(double x, double y, MouseEvent mouseEvent) {
    }

    public default void onMouseMovedEvent(double x, double y, MouseEvent mouseEvent) {
    }

    public default void onMouseUpEvent(double x, double y, MouseEvent mouseEvent) {
    }

    public default void onMouseDraggedEvent(double x, double y, MouseEvent e) {
    }

    public ResultType getResultType();

    public void draw(GraphicsContext canvas);

    public T getResult();

}
