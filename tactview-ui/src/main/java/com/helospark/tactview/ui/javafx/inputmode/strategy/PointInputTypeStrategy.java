package com.helospark.tactview.ui.javafx.inputmode.strategy;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

import javafx.scene.canvas.GraphicsContext;

public class PointInputTypeStrategy implements InputTypeStrategy<Point> {
    private Point result;
    private boolean done = false;

    @Override
    public void onMouseDraggedEvent(StrategyInput input) {
        double x = input.x;
        double y = input.y;
        result = new Point(x, y);
    }

    @Override
    public void onMouseUpEvent(StrategyInput input) {
        double x = input.x;
        double y = input.y;
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
            canvas.strokeLine(result.x - 10, result.y, result.x + 10, result.y);
            canvas.strokeLine(result.x, result.y - 10, result.x, result.y + 10);
        }
    }

    @Override
    public Point getResult() {
        return result;
    }

}
