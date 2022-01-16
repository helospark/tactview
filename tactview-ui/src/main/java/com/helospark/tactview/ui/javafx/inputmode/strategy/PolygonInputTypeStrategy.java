package com.helospark.tactview.ui.javafx.inputmode.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

public class PolygonInputTypeStrategy implements InputTypeStrategy<Polygon> {
    private List<Point> result = new ArrayList<>();
    private boolean isFinished = false;
    private boolean isMouseCloseToFirstPoint = false;
    private boolean finishOnRightClick = false;

    private Optional<Integer> pointIndexToUpdate = Optional.empty();
    private Optional<Integer> mouseCloseTo = Optional.empty();

    public PolygonInputTypeStrategy() {
        this.finishOnRightClick = false;
    }

    public PolygonInputTypeStrategy(List<Point> prefilledPoints) {
        for (Point point : prefilledPoints) {
            result.add(point.deepClone());
        }
        finishOnRightClick = true;
    }

    @Override
    public void onMouseDownEvent(StrategyMouseInput input) {
        if (input.mouseEvent.isSecondaryButtonDown()) {
            isFinished = true;
        } else {
            double x = input.x;
            double y = input.y;

            pointIndexToUpdate = findPointNear(new Point(input.x, input.y));

            if (input.mouseEvent.isMiddleButtonDown()) {
                if (pointIndexToUpdate.isPresent()) {
                    result.remove(pointIndexToUpdate.get().intValue());
                }
                return;
            }

            if (isCloseToFirstPoint(new Point(input.x, input.y)) && !finishOnRightClick) {
                isFinished = true;
            } else {

                if (pointIndexToUpdate.isPresent()) {
                    int index = pointIndexToUpdate.get();
                    updatePointAtIndex(input, index);
                } else {
                    Point currentPoint = new Point(x, y);
                    if (isCloseToFirstPoint(new Point(input.x, input.y)) && !finishOnRightClick) {
                        isFinished = true;
                    } else {
                        result.add(currentPoint);
                    }
                }
            }
        }
    }

    private void updatePointAtIndex(StrategyMouseInput input, int index) {
        result.get(index).x = input.x;
        result.get(index).y = input.y;
    }

    private Optional<Integer> findPointNear(Point point) {
        for (int i = 0; i < result.size(); ++i) {
            if (isTwoPointsClose(result.get(i), point)) {
                return Optional.ofNullable(i);
            }
        }
        return Optional.empty();
    }

    private boolean isCloseToFirstPoint(Point currentPoint) {
        return result.size() > 2 && isTwoPointsClose(result.get(0), currentPoint);
    }

    @Override
    public void onMouseUpEvent(StrategyMouseInput input) {
        pointIndexToUpdate = Optional.empty();
    }

    @Override
    public void onMouseDraggedEvent(StrategyMouseInput input) {
        if (pointIndexToUpdate.isPresent()) {
            updatePointAtIndex(input, pointIndexToUpdate.get());
            isMouseCloseToFirstPoint = false;
        }
    }

    @Override
    public void onMouseMovedEvent(StrategyMouseInput input) {
        isMouseCloseToFirstPoint = isCloseToFirstPoint(new Point(input.x, input.y));
        mouseCloseTo = findPointNear(new Point(input.x, input.y));
    }

    private boolean isTwoPointsClose(Point point, Point currentPoint) {
        return point.distanceFrom(currentPoint) < 0.05;
    }

    //    @Override
    //    public void onKeyReleasedEvent(StrategyKeyInput input) {
    //        if (input.getKeyEvent().getCode().equals(KeyCode.ENTER)) {
    //            isFinished = true;
    //        }
    //    }

    @Override
    public ResultType getResultType() {
        if (isFinished) {
            return ResultType.DONE;
        } else if (result.size() > 2) {
            return ResultType.PARTIAL;
        } else {
            return ResultType.NONE;
        }
    }

    @Override
    public void draw(DrawRequestParameter parameterObject) {
        double translateX = parameterObject.getCanvasTranslateX();
        double translateY = parameterObject.getCanvasTranslateY();
        parameterObject.getCanvas().setFill(Color.BLUE);
        parameterObject.getCanvas().setStroke(Color.BLUE);
        Point previous = null;
        for (int i = 0; i < result.size(); ++i) {
            Point current = result.get(i).multiply(parameterObject.getWidth(), parameterObject.getHeight());
            int diameter = 10;

            if (isMouseCloseToFirstPoint && i == 0 && !finishOnRightClick) {
                diameter = 20;
            }

            if (mouseCloseTo.isPresent() && mouseCloseTo.get().equals(i)) {
                parameterObject.getCanvas().setFill(Color.RED);
            } else {
                parameterObject.getCanvas().setFill(Color.BLUE);
            }

            parameterObject.getCanvas().fillOval(current.x - diameter / 2 + translateX, current.y - diameter / 2 + translateY, diameter, diameter);
            if (previous != null) {
                parameterObject.getCanvas().strokeLine(previous.x + translateX, previous.y + translateY, current.x + translateX, current.y + translateY);
            }
            previous = current;
        }
    }

    @Override
    public Polygon getResult() {
        return new Polygon(result);
    }

    @Override
    public void onKeyPressedEvent(StrategyKeyInput input) {
        if (input.getKeyCode().equals(KeyCode.ENTER)) {
            isFinished = true;
        }
    }

}
