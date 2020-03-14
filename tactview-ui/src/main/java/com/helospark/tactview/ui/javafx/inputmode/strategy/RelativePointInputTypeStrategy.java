package com.helospark.tactview.ui.javafx.inputmode.strategy;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class RelativePointInputTypeStrategy implements InputTypeStrategy<Point> {
    private Point result;

    private Point originalPoint;

    private Point lastPoint;
    private Point currentPoint;

    private boolean done = false;

    public RelativePointInputTypeStrategy(Point originalPoint) {
        this.originalPoint = originalPoint;
    }

    @Override
    public void onMouseDownEvent(StrategyMouseInput input) {
        lastPoint = new Point(input.x, input.y);
        currentPoint = null;
    }

    @Override
    public void onMouseDraggedEvent(StrategyMouseInput input) {
        computeResultBasedOnRelativeMotion(input);
    }

    private void computeResultBasedOnRelativeMotion(StrategyMouseInput input) {
        double x = input.x;
        double y = input.y;
        currentPoint = new Point(x, y);

        Point relativeMotion = currentPoint.subtract(lastPoint);
        result = originalPoint.add(relativeMotion);
    }

    @Override
    public void onMouseUpEvent(StrategyMouseInput input) {
        if (lastPoint != null) {
            computeResultBasedOnRelativeMotion(input);
        } else {
            computeResultForAbsolutePosition(input);
        }
        done = true;
    }

    private void computeResultForAbsolutePosition(StrategyMouseInput input) {
        double x = input.x;
        double y = input.y;
        result = new Point(x, y);
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
        //        if (unscaledResult != null) {
        //            parameterObject.getCanvas().strokeLine(unscaledResult.x - 10, unscaledResult.y, unscaledResult.x + 10, unscaledResult.y);
        //            parameterObject.getCanvas().strokeLine(unscaledResult.x, unscaledResult.y - 10, unscaledResult.x, unscaledResult.y + 10);
        //        }
    }

    @Override
    public Point getResult() {
        return result;
    }

}
