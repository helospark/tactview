package com.helospark.tactview.ui.javafx.inputmode.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygon;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygonPoint;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.SplinePolygonType;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

public class BezierPolygonInputTypeStrategy implements InputTypeStrategy<BezierPolygon> {
    private List<BezierPolygonPoint> result = new ArrayList<>();
    private boolean isFinished = false;
    private boolean isMouseCloseToFirstPoint = false;
    private boolean finishOnRightClick = false;

    private Optional<Integer> pointIndexToUpdate = Optional.empty();
    private Optional<Integer> mouseCloseTo = Optional.empty();

    public BezierPolygonInputTypeStrategy() {
        this.finishOnRightClick = false;
    }

    public BezierPolygonInputTypeStrategy(List<BezierPolygonPoint> points) {
        for (BezierPolygonPoint point : points) {
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
                    int indexToRemove = pointIndexToUpdate.get();
                    result.remove(indexToRemove);
                    if (indexToRemove < result.size() && result.get(indexToRemove).type == SplinePolygonType.SPLINE) { // next point
                        result.remove(indexToRemove);
                    }
                }
                return;
            }

            if (isCloseToFirstPoint(new Point(input.x, input.y)) && !finishOnRightClick) {
                if (result.size() > 1) {
                    BezierPolygonPoint controlPoint = createControlPoint(result.get(0), result.get(result.size() - 1));
                    result.add(controlPoint);
                }
                isFinished = true;
            } else {

                if (pointIndexToUpdate.isPresent()) {
                    int index = pointIndexToUpdate.get();
                    updatePointAtIndex(input, index);
                } else {
                    BezierPolygonPoint currentPoint = new BezierPolygonPoint(x, y, SplinePolygonType.POINT);
                    if (result.size() >= 1) {
                        BezierPolygonPoint controlPoint = createControlPoint(currentPoint, result.get(result.size() - 1));
                        result.add(controlPoint);
                    }
                    result.add(currentPoint);
                }
            }
        }
    }

    private BezierPolygonPoint createControlPoint(BezierPolygonPoint bezierPolygonPoint, BezierPolygonPoint bezierPolygonPoint2) {
        Point p = calculate(pointFromBezierPoint(bezierPolygonPoint), pointFromBezierPoint(bezierPolygonPoint2));
        return new BezierPolygonPoint(p.x, p.y, SplinePolygonType.SPLINE);
    }

    private Point calculate(Point currentPoint, Point nextPoint) {
        Point s = nextPoint.subtract(currentPoint);
        Point normalizedDirection = s.normalize();
        Point startPosition = currentPoint.add(normalizedDirection.scalarMultiply(s.length() / 2.0));
        Point splineP = startPosition.add(new Point(-normalizedDirection.y, normalizedDirection.x).scalarMultiply(0.03));
        return splineP;
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

    private boolean isTwoPointsClose(BezierPolygonPoint bezierPolygonPoint, Point currentPoint) {
        return new Point(bezierPolygonPoint.x, bezierPolygonPoint.y).distanceFrom(currentPoint) < 0.05;
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
    public void draw(GraphicsContext canvas, int width, int height) {
        canvas.setFill(Color.BLUE);
        canvas.setStroke(Color.BLUE);
        Point previousRealPoint = null;
        for (int i = 0; i < result.size(); ++i) {
            BezierPolygonPoint currentBezier = result.get(i);
            Point current = pointFromBezierPoint(currentBezier).multiply(width, height);
            int diameter = 10;

            if (isMouseCloseToFirstPoint && i == 0 && !finishOnRightClick) {
                diameter = 20;
            }

            if (mouseCloseTo.isPresent() && mouseCloseTo.get().equals(i)) {
                canvas.setFill(Color.RED);
            } else {
                if (currentBezier.type == SplinePolygonType.SPLINE) {
                    canvas.setFill(Color.GRAY);
                } else {
                    canvas.setFill(Color.BLUE);
                }
            }

            canvas.fillOval(current.x - diameter / 2, current.y - diameter / 2, diameter, diameter);

            if (currentBezier.type == SplinePolygonType.SPLINE) {
                canvas.setStroke(Color.GRAY);
            } else {
                canvas.setStroke(Color.BLUE);
            }

            if (previousRealPoint != null) {
                canvas.strokeLine(previousRealPoint.x, previousRealPoint.y, current.x, current.y);
            }
            if (currentBezier.type == SplinePolygonType.POINT) {
                previousRealPoint = current;
            }
        }
    }

    private Point pointFromBezierPoint(BezierPolygonPoint bezierPolygonPoint) {
        return new Point(bezierPolygonPoint.x, bezierPolygonPoint.y);
    }

    @Override
    public BezierPolygon getResult() {
        return new BezierPolygon(result);
    }

    @Override
    public void onKeyPressedEvent(StrategyKeyInput input) {
        if (input.getKeyCode().equals(KeyCode.ENTER)) {
            isFinished = true;
        }
    }

}
