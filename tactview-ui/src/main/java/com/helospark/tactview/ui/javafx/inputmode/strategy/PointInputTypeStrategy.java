package com.helospark.tactview.ui.javafx.inputmode.strategy;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

public class PointInputTypeStrategy implements InputTypeStrategy<Point> {
    private Point result;
    private boolean done = false;

    @Override
    public void onMouseDraggedEvent(int x, int y, MouseEvent e) {
        result = new Point(x, y);
    }

    @Override
    public void onMouseUpEvent(int x, int y, MouseEvent mouseEvent) {
        result = new Point(x, y);
        done = true;
    }

    @Override
    public ResultType getResultType() {
        if (done) {
            return ResultType.DONE;
        } else if (result != null) {
            return ResultType.PARTIAL;
        } else {
            return ResultType.NONE;
        }
    }

    @Override
    public void draw(GraphicsContext canvas) {
        if (result != null) {
            canvas.strokeLine((int) result.x - 10, (int) result.y, (int) result.x + 10, (int) result.y);
            canvas.strokeLine((int) result.x, (int) result.y - 10, (int) result.x, (int) result.y + 10);
        }
    }

    @Override
    public Point getResult() {
        return result;
    }

}
