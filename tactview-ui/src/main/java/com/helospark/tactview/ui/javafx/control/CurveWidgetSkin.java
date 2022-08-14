package com.helospark.tactview.ui.javafx.control;

import java.util.ArrayList;
import java.util.List;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider.KeyFrameInfo;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider.KnotAwareUnivariateFunction;

import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class CurveWidgetSkin extends SkinBase<CurveWidget> {
    private static final int CONTROL_POINT_SIZE = 5;

    private final ObjectProperty<KnotAwareUnivariateFunction> curveProperty;
    private final ObjectProperty<KeyFrameInfo> onActionProvider;

    private Canvas canvas;
    private int currentWidth, currentHeight;

    private final List<Point> controlPoints = new ArrayList<>();

    private int lastDraggedIndex = -1;

    CurveWidget control;

    protected CurveWidgetSkin(CurveWidget control) {
        super(control);
        this.control = control;
        curveProperty = control.getCurveProperty();
        onActionProvider = control.onActionProperty();

        createCurveWidget(300, 150);

        curveProperty.addListener((e, oldValue, newValue) -> {
            updateCanvas(canvas);
        });
    }

    private void createCurveWidget(int width, int height) {
        canvas = new Canvas(width, height);

        canvas.setOnMouseClicked(e -> {
            handleMouseClickedEvent(e);
        });
        canvas.setOnMouseDragged(e -> {
            handleMouseDraggedEvent(e);
        });
        canvas.setOnMousePressed(e -> {
            Point mouse = new Point(e.getX(), e.getY());
            lastDraggedIndex = findIndexCloseTo(mouse);
        });

        updateCanvas(canvas);

        getChildren().setAll(canvas);

        currentWidth = width;
        currentHeight = height;
    }

    private void handleMouseDraggedEvent(MouseEvent e) {
        Point mouse = new Point(e.getX(), e.getY());
        int currentlyDraggedPointIndex = -1;

        if (lastDraggedIndex >= 0 &&
                lastDraggedIndex < controlPoints.size()) {
            currentlyDraggedPointIndex = lastDraggedIndex;
        }

        if (currentlyDraggedPointIndex == -1) {
            currentlyDraggedPointIndex = findIndexCloseTo(mouse);
        }

        if (currentlyDraggedPointIndex != -1) {
            Point pointToChange = controlPoints.get(currentlyDraggedPointIndex);
            pointToChange.x = mouse.x;
            pointToChange.y = mouse.y;

            Point newPoint = new Point(pointToChange.x / canvas.getWidth() + control.getMinX(),
                    (canvas.getHeight() - pointToChange.y) / canvas.getHeight() + control.getMinY());

            onActionProvider.set(new KeyFrameInfo(false, currentlyDraggedPointIndex, newPoint.x, newPoint.y));

        }
        lastDraggedIndex = currentlyDraggedPointIndex;
    }

    private int findIndexCloseTo(Point mouse) {
        int currentlyDraggedPointIndex = -1;
        for (int i = 0; i < controlPoints.size(); ++i) {
            if (isClose(controlPoints.get(i), mouse, 6)) {
                currentlyDraggedPointIndex = i;
            }
        }
        return currentlyDraggedPointIndex;
    }

    private boolean isClose(Point point, Point mouse, int distance) {
        return point.distanceFrom(mouse) < distance;
    }

    private void handleMouseClickedEvent(MouseEvent e) {
        lastDraggedIndex = -1;
        if (e.getClickCount() == 2) {
            int index = 0;
            for (index = 0; index < controlPoints.size(); ++index) {
                if (controlPoints.get(index).x >= e.getX()) {
                    break;
                }
            }

            for (int i = 0; i < controlPoints.size(); ++i) {
                if (isTooClose(controlPoints.get(i).x, e.getX())) {
                    return;
                }
            }

            Point newPoint = new Point(e.getX() / canvas.getWidth() + control.getMinX(),
                    (canvas.getHeight() - e.getY()) / canvas.getHeight() + control.getMinY());

            onActionProvider.set(new KeyFrameInfo(true, index, newPoint.x, newPoint.y));
        }
    }

    private boolean isTooClose(double x, double x2) {
        return Math.abs(x - x2) < 1.0;
    }

    private void updateCanvas(Canvas canvas) {
        GraphicsContext graphics = canvas.getGraphicsContext2D();

        graphics.setFill(javafx.scene.paint.Color.BLACK);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        createGrid(graphics);

        KnotAwareUnivariateFunction curve = curveProperty.getValue();
        Point previousPoint = null;

        double xScale = (control.getMaxX() - control.getMinX()) / canvas.getWidth();
        double yScale = (control.getMaxY() - control.getMinY()) * canvas.getHeight();

        graphics.setStroke(javafx.scene.paint.Color.WHITE);
        for (int i = 0; i < canvas.getWidth(); ++i) {
            double value = i * xScale;
            Point currentPoint = getPoint(curve, canvas, xScale, yScale, value);

            if (previousPoint != null) {
                graphics.strokeLine(previousPoint.x, previousPoint.y, currentPoint.x, currentPoint.y);
            }
            previousPoint = currentPoint;
        }

        controlPoints.clear();
        graphics.setFill(Color.GRAY);
        for (double knot : curve.getKnots()) {
            Point currentPoint = getPoint(curve, canvas, xScale, yScale, knot);

            graphics.fillOval(currentPoint.x - CONTROL_POINT_SIZE, currentPoint.y - CONTROL_POINT_SIZE, CONTROL_POINT_SIZE * 2, CONTROL_POINT_SIZE * 2);

            controlPoints.add(currentPoint);
        }

    }

    private void createGrid(GraphicsContext graphics) {
        double xInc = canvas.getWidth() / 8;
        double yInc = canvas.getHeight() / 8;

        graphics.setStroke(Color.gray(0.1));

        for (int i = 0; i < 8; ++i) {
            graphics.strokeLine(i * xInc, 0, i * xInc, canvas.getHeight());
            graphics.strokeLine(0, i * yInc, canvas.getWidth(), i * yInc);
        }
    }

    private Point getPoint(KnotAwareUnivariateFunction curve, Canvas canvas, double xScale, double yScale, double value) {
        double x = value + control.getMinX();
        double yValue = canvas.getHeight() - curve.value(x) * yScale;
        Point currentPoint = new Point(x / xScale, yValue);
        return currentPoint;
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        if (currentWidth != (int) contentWidth && currentHeight != (int) contentHeight) {
            createCurveWidget((int) contentWidth, (int) contentHeight);
        }
    }

}
