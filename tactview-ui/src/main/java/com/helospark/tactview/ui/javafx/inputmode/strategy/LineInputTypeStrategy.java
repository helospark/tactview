package com.helospark.tactview.ui.javafx.inputmode.strategy;

import java.util.Optional;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
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

                if (shouldKeepDiagonal(input)) {
                    double xDiff = result.end.x - result.start.x;
                    double yDiff = result.end.y - result.start.y;
                    double distance = Math.max(Math.abs(xDiff), Math.abs(yDiff));

                    result.start.x = result.end.x - Math.signum(xDiff) * distance;
                    result.start.y = result.end.y - Math.signum(yDiff) * distance;
                }

            } else if (activeIndex.equals(ENDPOINT_ID)) {
                result.end = currentPoint;

                if (shouldKeepDiagonal(input)) {
                    double xDiff = result.end.x - result.start.x;
                    double yDiff = result.end.y - result.start.y;
                    double distance = Math.max(Math.abs(xDiff), Math.abs(yDiff));

                    result.end.x = result.start.x + Math.signum(xDiff) * distance;
                    result.end.y = result.start.y + Math.signum(yDiff) * distance;
                }

            }
        }
    }

    private boolean shouldKeepDiagonal(StrategyMouseInput input) {
        return input.currentlyPressedKeyRepository.isKeyDown(KeyCode.CONTROL);
    }

    private boolean isTwoPointsClose(Point point, Point currentPoint) {
        return point.distanceFrom(currentPoint) < 0.05;
    }

    @Override
    public void onMouseMovedEvent(StrategyMouseInput input) {
        Point currentPoint = new Point(input.x, input.y);
        System.out.println(currentPoint);
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
    public void draw(DrawRequestParameter parameterObject) {
        Point translate = new Point(parameterObject.getCanvasTranslateX(), parameterObject.getCanvasTranslateY());
        drawPoint(parameterObject.getCanvas(), result.start.multiply(parameterObject.getWidth(), parameterObject.getHeight()).add(translate), START_POINT_ID);
        drawPoint(parameterObject.getCanvas(), result.end.multiply(parameterObject.getWidth(), parameterObject.getHeight()).add(translate), ENDPOINT_ID);

        parameterObject.getCanvas().strokeLine(result.start.x, result.start.y, result.end.x, result.end.y);
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

    @Override
    public void onKeyPressedEvent(StrategyKeyInput input) {
        if (input.getKeyCode().equals(KeyCode.ENTER)) {
            isFinished = true;
        }
    }

    @Override
    public String getStatusMessage() {
        return InputTypeStrategy.super.getStatusMessage() + "\nCtrl to keep diagonal";
    }
}
