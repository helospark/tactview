package com.helospark.tactview.ui.javafx.inputmode.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Rectangle;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RectangleInputTypeStrategy implements InputTypeStrategy<Rectangle> {
    private List<Point> result = new ArrayList<>();
    private boolean isFinished = false;
    private boolean isMouseCloseToFirstPoint = false;

    private Optional<Integer> pointIndexToUpdate = Optional.empty();
    private Optional<Integer> mouseCloseTo = Optional.empty();

    public RectangleInputTypeStrategy(List<Point> prefilledPoints) {
        for (Point point : prefilledPoints) {
            result.add(point.deepClone());
        }
    }

    @Override
    public void onMouseDownEvent(StrategyMouseInput input) {
        if (input.mouseEvent.isSecondaryButtonDown()) {
            isFinished = true;
        } else {
            pointIndexToUpdate = findPointNear(new Point(input.x, input.y));

            if (pointIndexToUpdate.isPresent()) {
                int index = pointIndexToUpdate.get();
                updatePointAtIndex(input, index);
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
        return point.distanceFrom(currentPoint) < 0.1;
    }

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
        Point previous = null;
        for (int i = 0; i < result.size(); ++i) {
            Point current = result.get(i).multiply(width, height);
            int diameter = 10;

            if (isMouseCloseToFirstPoint && i == 0) {
                diameter = 20;
            }

            if (mouseCloseTo.isPresent() && mouseCloseTo.get().equals(i)) {
                canvas.setFill(Color.RED);
            } else {
                canvas.setFill(Color.BLUE);
            }

            canvas.fillOval(current.x - diameter / 2, current.y - diameter / 2, diameter, diameter);
            if (previous != null) {
                canvas.strokeLine(previous.x, previous.y, current.x, current.y);
            }
            previous = current;
        }
    }

    @Override
    public Rectangle getResult() {
        return new Rectangle(result);
    }

}
