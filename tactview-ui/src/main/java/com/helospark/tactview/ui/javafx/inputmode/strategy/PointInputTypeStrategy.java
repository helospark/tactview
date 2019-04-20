package com.helospark.tactview.ui.javafx.inputmode.strategy;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class PointInputTypeStrategy implements InputTypeStrategy<Point> {
    private Point result;
    private boolean done = false;

    @Override
    public void onMouseDraggedEvent(StrategyMouseInput input) {
        double x = input.x;
        double y = input.y;
        result = new Point(x, y);
    }

    @Override
    public void onMouseUpEvent(StrategyMouseInput input) {
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
    public void draw(DrawRequestParameter parameterObject) {
        if (result != null) {
            parameterObject.getCanvas().strokeLine(result.x - 10, result.y, result.x + 10, result.y);
            parameterObject.getCanvas().strokeLine(result.x, result.y - 10, result.x, result.y + 10);
        }
    }

    @Override
    public Point getResult() {
        return result;
    }

}
