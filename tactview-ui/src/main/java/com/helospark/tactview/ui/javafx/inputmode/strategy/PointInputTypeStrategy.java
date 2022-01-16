package com.helospark.tactview.ui.javafx.inputmode.strategy;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class PointInputTypeStrategy implements InputTypeStrategy<Point> {
    private Point result;
    private Point unscaledResult;
    private boolean done = false;

    @Override
    public void onMouseDraggedEvent(StrategyMouseInput input) {
        double x = input.x;
        double y = input.y;
        result = new Point(x, y);
        unscaledResult = new Point(input.unscaledX, input.unscaledY);
    }

    @Override
    public void onMouseUpEvent(StrategyMouseInput input) {
        double x = input.x;
        double y = input.y;
        result = new Point(x, y);
        unscaledResult = new Point(input.unscaledX, input.unscaledY);
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
        double translateX = parameterObject.getCanvasTranslateX();
        double translateY = parameterObject.getCanvasTranslateY();
        if (unscaledResult != null) {
            parameterObject.getCanvas().strokeLine(unscaledResult.x - 10 + translateX, unscaledResult.y + translateY, unscaledResult.x + 10 + translateX, unscaledResult.y + translateY);
            parameterObject.getCanvas().strokeLine(unscaledResult.x + translateX, unscaledResult.y - 10 + translateY, unscaledResult.x + translateX, unscaledResult.y + 10 + translateY);
        }
    }

    @Override
    public Point getResult() {
        return result;
    }

}
