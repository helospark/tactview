package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.math.BigDecimal;
import java.util.List;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class AbstractGeneralPointBasedCurveEditor extends AbstractNoOpCurveEditor {
    private int draggedIndex = -1;
    private int closeIndex = -1;

    @Override
    public boolean onMouseMoved(CurveEditorMouseRequest mouseEvent) {
        int previous = closeIndex;
        closeIndex = getElementIndex(mouseEvent);
        return draggedIndex != previous;
    }

    @Override
    public boolean onMouseDown(CurveEditorMouseRequest mouseEvent) {
        draggedIndex = getElementIndex(mouseEvent);
        return false;
    }

    private int getElementIndex(CurveEditorMouseRequest mouseEvent) {
        List<KeyframePoint> bezierValues = getKeyframePoints((KeyframeSupportingDoubleInterpolator) mouseEvent.currentKeyframeableEffect);
        for (int i = 0; i < bezierValues.size(); ++i) {
            KeyframePoint element = bezierValues.get(i);

            Point actualPoint = new Point(element.timelinePosition.getSeconds().doubleValue(), element.value);

            Point actualPointInScreenSpace = remapPointToScreenSpace(mouseEvent, actualPoint);

            if (isClose(actualPointInScreenSpace, mouseEvent.screenMousePosition)) {
                return i;
            }
        }
        return -1;
    }

    protected boolean isClose(Point controlPointOut, Point remappedMousePosition) {
        return controlPointOut.distanceFrom(remappedMousePosition) < 12;
    }

    @Override
    public boolean onMouseDragged(CurveEditorMouseRequest mouseEvent) {
        if (draggedIndex == -1) {
            return false;
        }

        KeyframeSupportingDoubleInterpolator effect = (KeyframeSupportingDoubleInterpolator) mouseEvent.currentKeyframeableEffect;

        List<KeyframePoint> keyframePoints = getKeyframePoints(effect);

        KeyframePoint pointToModify = keyframePoints.get(draggedIndex);

        if (pointToModify != null) {
            TimelinePosition newTime = pointToModify.timelinePosition.add(new BigDecimal(mouseEvent.mouseDelta.x));
            double newValue = pointToModify.value + mouseEvent.mouseDelta.y;
            valueModifiedAt((KeyframeSupportingDoubleInterpolator) mouseEvent.currentKeyframeableEffect, pointToModify.timelinePosition, newTime, newValue);
            return true;
        }
        return false;
    }

    protected abstract void valueModifiedAt(KeyframeSupportingDoubleInterpolator currentKeyframeableEffect, TimelinePosition timelinePosition, TimelinePosition newTime, double newValue);

    protected boolean isClose(KeyframePoint keyframePoint, Point remappedMousePosition) {
        Point newPoint = new Point(keyframePoint.timelinePosition.getSeconds().doubleValue(), keyframePoint.value);
        double distance = newPoint.distanceFrom(remappedMousePosition);
        System.out.println("Point to modify " + newPoint + " " + remappedMousePosition + " " + distance);
        return distance < 10;
    }

    protected abstract List<KeyframePoint> getKeyframePoints(KeyframeSupportingDoubleInterpolator effect);

    public static class KeyframePoint {
        TimelinePosition timelinePosition;
        double value;

        public KeyframePoint(TimelinePosition timelinePosition, double value) {
            this.timelinePosition = timelinePosition;
            this.value = value;
        }

    }

    @Override
    public void drawAdditionalUi(CurveDrawRequest drawRequest) {
        List<KeyframePoint> keyframes = getKeyframePoints((KeyframeSupportingDoubleInterpolator) drawRequest.currentKeyframeableEffect);

        GraphicsContext graphics = drawRequest.graphics;
        graphics.setFill(Color.BLUE);

        int i = 0;
        for (var keyframe : keyframes) {
            Point screenSpacePosition = remapPointToScreenSpace(drawRequest, new Point(keyframe.timelinePosition.getSeconds().doubleValue(), keyframe.value));

            if (i == closeIndex) {
                graphics.setFill(Color.RED);
            } else {
                graphics.setFill(Color.BLUE);
            }

            if (screenSpacePosition.x >= 0 && screenSpacePosition.y >= 0 && screenSpacePosition.x < drawRequest.canvas.getWidth() && screenSpacePosition.y < drawRequest.canvas.getHeight()) {
                graphics.fillOval(screenSpacePosition.x - 3, screenSpacePosition.y - 3, 6, 6);
            }
            ++i;
        }
    }

    protected Point remapPointToScreenSpace(AbstractCurveEditorRequest request, Point point) {
        double screenX = ((point.x - request.curveViewerOffsetSeconds) * (1.0 / request.secondsPerPixel));
        double screenY = request.height - (((point.y - request.minValue) * request.displayScale));

        return new Point(screenX, screenY);
    }
}
