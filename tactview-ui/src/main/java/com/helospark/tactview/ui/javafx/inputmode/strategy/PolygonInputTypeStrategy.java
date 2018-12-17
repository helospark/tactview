package com.helospark.tactview.ui.javafx.inputmode.strategy;

import java.util.ArrayList;
import java.util.List;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PolygonInputTypeStrategy implements InputTypeStrategy<Polygon> {
    private List<Point> unscaledPositions = new ArrayList<>(); // only for drawing
    private List<Point> result = new ArrayList<>();
    private boolean isPolygonClosed = false;
    private boolean isMouseCloseToFirstPoint = false;

    @Override
    public void onMouseDownEvent(StrategyInput input) {
        double x = input.x;
        double y = input.y;
        Point currentPoint = new Point(x, y);
        if (isCloseToFirstPoint(new Point(input.unscaledX, input.unscaledY))) {
            isPolygonClosed = true;
        } else {
            result.add(currentPoint);
            unscaledPositions.add(new Point(input.unscaledX, input.unscaledY));
        }
    }

    private boolean isCloseToFirstPoint(Point currentPoint) {
        return unscaledPositions.size() > 2 && isTwoPointsClose(unscaledPositions.get(0), currentPoint);
    }

    @Override
    public void onMouseMovedEvent(StrategyInput input) {
        isMouseCloseToFirstPoint = isCloseToFirstPoint(new Point(input.unscaledX, input.unscaledY));
    }

    private boolean isTwoPointsClose(Point point, Point currentPoint) {
        return point.distanceFrom(currentPoint) < 15;
    }

    @Override
    public ResultType getResultType() {
        if (isPolygonClosed) {
            return ResultType.DONE;
        } else if (result.size() > 2) {
            return ResultType.PARTIAL;
        } else {
            return ResultType.NONE;
        }
    }

    @Override
    public void draw(GraphicsContext canvas) {
        canvas.setFill(Color.BLUE);
        canvas.setStroke(Color.BLUE);
        Point previous = null;
        for (int i = 0; i < unscaledPositions.size(); ++i) {
            Point current = unscaledPositions.get(i);
            int diameter = 10;

            if (isMouseCloseToFirstPoint && i == 0) {
                diameter = 20;
            }

            canvas.fillOval(current.x - diameter / 2, current.y - diameter / 2, diameter, diameter);
            if (previous != null) {
                canvas.strokeLine(previous.x, previous.y, current.x, current.y);
            }
            previous = current;
        }
    }

    @Override
    public Polygon getResult() {
        return new Polygon(result);
    }

}
