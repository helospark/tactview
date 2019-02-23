package com.helospark.tactview.ui.javafx.inputmode.strategy;

import java.util.Optional;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class LineInputTypeStrategy implements InputTypeStrategy<InterpolationLine> {
    private static final int ENDPOINT_ID = 1;
    private static final int START_POINT_ID = 0;
    private InterpolationLine result;
    private boolean isFinished = false;
    private Optional<Integer> mouseCloseTo = Optional.empty();

    public LineInputTypeStrategy(InterpolationLine line) {
        this.result = line.deepClone();
    }

    @Override
    public void onMouseDownEvent(StrategyMouseInput input) {
        if (input.mouseEvent.isSecondaryButtonDown()) {
            isFinished = true;
        } else if (!mouseCloseTo.isPresent()) {
            result.start = new Point(input.x, input.y);
            result.end = new Point(input.x, input.y);
            mouseCloseTo = Optional.ofNullable(1);
        }
    }

    @Override
    public void onMouseDraggedEvent(StrategyMouseInput input) {
        if (mouseCloseTo.isPresent()) {
            Point currentPoint = new Point(input.x, input.y);
            Integer activeIndex = mouseCloseTo.get();
            if (activeIndex.equals(START_POINT_ID)) {
                result.start = currentPoint;
            } else if (activeIndex.equals(ENDPOINT_ID)) {
                result.end = currentPoint;
            }
        }
    }

    private boolean isTwoPointsClose(Point point, Point currentPoint) {
        return point.distanceFrom(currentPoint) < 0.05;
    }

    @Override
    public void onMouseMovedEvent(StrategyMouseInput input) {
        Point currentPoint = new Point(input.x, input.y);
        if (isTwoPointsClose(currentPoint, result.start)) {
            mouseCloseTo = Optional.of(START_POINT_ID);
        } else if (isTwoPointsClose(currentPoint, result.end)) {
            mouseCloseTo = Optional.of(ENDPOINT_ID);
        } else {
            mouseCloseTo = Optional.empty();
        }
    }

    @Override
    public ResultType getResultType() {
        if (isFinished) {
            return ResultType.DONE;
        } else {
            return ResultType.PARTIAL;
        }
    }

    @Override
    public void draw(GraphicsContext canvas, int width, int height) {
        drawPoint(canvas, result.start.multiply(width, height), START_POINT_ID);
        drawPoint(canvas, result.end.multiply(width, height), ENDPOINT_ID);

        canvas.strokeLine(result.start.x, result.start.y, result.end.x, result.end.y);
    }

    private void drawPoint(GraphicsContext canvas, Point point, int id) {
        canvas.setFill(Color.BLUE);
        canvas.setStroke(Color.BLUE);
        int diameter = 10;
        boolean isActive = mouseCloseTo.map(a -> a.equals(id)).orElse(false);
        if (isActive) {
            canvas.setFill(Color.RED);
        } else {
            canvas.setFill(Color.BLUE);
        }
        canvas.fillOval(point.x - diameter / 2, point.y - diameter / 2, diameter, diameter);
    }

    @Override
    public InterpolationLine getResult() {
        return result;
    }

}
