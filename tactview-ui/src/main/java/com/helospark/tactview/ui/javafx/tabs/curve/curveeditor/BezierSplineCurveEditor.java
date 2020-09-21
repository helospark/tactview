package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.util.bezier.CubicBezierPoint;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;

@Component
public class BezierSplineCurveEditor extends AbstractGeneralPointBasedCurveEditor {
    private BezierMousePointDescriptor dragged = null;
    private BezierMousePointDescriptor close = null;

    public BezierSplineCurveEditor(UiCommandInterpreterService commandInterpreter, EffectParametersRepository effectParametersRepository) {
        super(commandInterpreter, effectParametersRepository);
    }

    @Override
    public boolean supports(EffectInterpolator interpolator) {
        return interpolator instanceof BezierDoubleInterpolator;
    }

    @Override
    public boolean onMouseMoved(CurveEditorMouseRequest mouseEvent) {
        BezierMousePointDescriptor previousClose = close;
        close = getElementUnderMouseOrNull(mouseEvent);
        return (close == null && close != previousClose) || (close != null && !close.equals(previousClose));
    }

    @Override
    public boolean onMouseDown(CurveEditorMouseRequest mouseEvent) {
        dragged = getElementUnderMouseOrNull(mouseEvent);
        return false;
    }

    private BezierMousePointDescriptor getElementUnderMouseOrNull(CurveEditorMouseRequest mouseEvent) {
        int i = 0;
        TreeMap<TimelinePosition, CubicBezierPoint> bezierValues = ((BezierDoubleInterpolator) mouseEvent.currentDoubleInterpolator).getBezierValues();
        for (var entry : bezierValues.entrySet()) {
            Point actualPoint = new Point(entry.getKey().getSeconds().doubleValue(), entry.getValue().value);

            Point actualPointInScreenSpace = remapPointToScreenSpace(mouseEvent, actualPoint);
            Point inPointInScreenSpace = remapPointToScreenSpace(mouseEvent, entry.getValue().controlPointIn.add(actualPoint));
            Point outPointInScreenSpace = remapPointToScreenSpace(mouseEvent, entry.getValue().controlPointOut.add(actualPoint));

            if (isClose(actualPointInScreenSpace, mouseEvent.screenMousePosition)) {
                BezierMousePointDescriptor result = new BezierMousePointDescriptor();
                result.draggedIndex = i;
                result.draggedPointType = PointType.VALUE;
                return result;
            } else if (isClose(outPointInScreenSpace, mouseEvent.screenMousePosition)) {
                BezierMousePointDescriptor result = new BezierMousePointDescriptor();
                result.draggedIndex = i;
                result.draggedPointType = PointType.OUT;
                return result;
            } else if (isClose(inPointInScreenSpace, mouseEvent.screenMousePosition)) {
                BezierMousePointDescriptor result = new BezierMousePointDescriptor();
                result.draggedIndex = i;
                result.draggedPointType = PointType.IN;
                return result;
            }
            ++i;
        }
        return null;
    }

    @Override
    public boolean onMouseUp(CurveEditorMouseRequest mouseEvent) {
        dragged = null;
        return false;
    }

    @Override
    public boolean onMouseDragged(CurveEditorMouseRequest mouseEvent) {
        if (dragged == null) {
            return false;
        }
        TreeMap<TimelinePosition, CubicBezierPoint> bezierValues = ((BezierDoubleInterpolator) mouseEvent.currentDoubleInterpolator).getBezierValues();

        CubicBezierPoint pointToModify = null;
        TimelinePosition positionToModify = null;

        int draggedIndex = dragged.draggedIndex;
        PointType draggedPointType = dragged.draggedPointType;

        int i = 0;
        for (var entry : bezierValues.entrySet()) {
            if (i == draggedIndex) {
                pointToModify = entry.getValue();
                positionToModify = entry.getKey();
            }
            ++i;
        }

        if (pointToModify != null) {
            TimelinePosition newTime = positionToModify.add(new BigDecimal(mouseEvent.mouseDelta.x));
            double newValue = pointToModify.value + mouseEvent.mouseDelta.y;
            double newPosition = mouseEvent.remappedMousePosition.y;

            double relativeX = newTime.getSeconds().doubleValue() - positionToModify.getSeconds().doubleValue();
            double relativeY = newValue - pointToModify.value;

            Point relativePoint = new Point(relativeX, relativeY);

            if (draggedPointType == PointType.IN) {
                ((BezierDoubleInterpolator) mouseEvent.currentDoubleInterpolator).updatedInControlPointAt(positionToModify, pointToModify.controlPointIn.add(relativePoint));
            } else if (draggedPointType == PointType.OUT) {
                ((BezierDoubleInterpolator) mouseEvent.currentDoubleInterpolator).updatedOutControlPointAt(positionToModify, pointToModify.controlPointOut.add(relativePoint));
            } else {
                ((BezierDoubleInterpolator) mouseEvent.currentDoubleInterpolator).updateDoubleValueAt(positionToModify, newTime, newPosition);
            }
            return true;
        }

        return false;
    }

    @Override
    public void drawAdditionalUi(CurveDrawRequest drawRequest) {
        TreeMap<TimelinePosition, CubicBezierPoint> bezierValues = ((BezierDoubleInterpolator) drawRequest.currentDoubleInterpolator).getBezierValues();

        int closeIndex = -1;
        PointType closePointType = null;
        if (close != null) {
            closeIndex = close.draggedIndex;
            closePointType = close.draggedPointType;
        }

        GraphicsContext graphics = drawRequest.graphics;
        graphics.setFill(Color.GRAY);
        graphics.setStroke(Color.GRAY);

        int i = 0;
        for (var entry : bezierValues.entrySet()) {
            double secondValue = entry.getKey().getSeconds().doubleValue();

            Point actualPoint = new Point(secondValue, entry.getValue().value);
            Point absolueInPoint = entry.getValue().controlPointIn.add(actualPoint);
            Point absolueOutPoint = entry.getValue().controlPointOut.add(actualPoint);

            Point screenSpaceCenterPosition = remapPointToScreenSpace(drawRequest, actualPoint);

            if (i == closeIndex && PointType.IN == closePointType) {
                graphics.setFill(Color.RED);
            } else {
                graphics.setFill(Color.GRAY);
            }
            drawControlPoint(drawRequest, screenSpaceCenterPosition, absolueInPoint);

            if (i == closeIndex && PointType.OUT == closePointType) {
                graphics.setFill(Color.RED);
            } else {
                graphics.setFill(Color.GRAY);
            }
            drawControlPoint(drawRequest, screenSpaceCenterPosition, absolueOutPoint);

            if (i == closeIndex && PointType.VALUE == closePointType) {
                graphics.setFill(Color.RED);
            } else {
                graphics.setFill(Color.BLUE);
            }
            drawPoint(graphics, screenSpaceCenterPosition);

            ++i;
        }
    }

    private void drawPoint(GraphicsContext graphics, Point screenSpaceCenterPosition) {
        graphics.fillOval(screenSpaceCenterPosition.x - 3, screenSpaceCenterPosition.y - 3, 6, 6);
    }

    private void drawControlPoint(CurveDrawRequest drawRequest, Point actualPoint, Point absolueInPoint) {
        Point screenSpaceInPosition = remapPointToScreenSpace(drawRequest, absolueInPoint);
        drawRequest.graphics.strokeLine(actualPoint.x, actualPoint.y, screenSpaceInPosition.x, screenSpaceInPosition.y);
        drawRequest.graphics.fillOval(screenSpaceInPosition.x - 3, screenSpaceInPosition.y - 3, 6, 6);
    }

    @Override
    protected void valueModifiedAt(KeyframeSupportingDoubleInterpolator currentKeyframeableEffect, TimelinePosition timelinePosition, TimelinePosition newTime, double newValue) {
        ((BezierDoubleInterpolator) currentKeyframeableEffect).updateDoubleValueAt(timelinePosition, newTime, newValue);
    }

    @Override
    protected List<KeyframePoint> getKeyframePoints(KeyframeSupportingDoubleInterpolator effect) {
        Map<TimelinePosition, Object> values = ((BezierDoubleInterpolator) effect).getValues();
        return values.entrySet()
                .stream()
                .map(entry -> new KeyframePoint(entry.getKey(), (Double) entry.getValue()))
                .collect(Collectors.toList());
    }

    static enum PointType {
        IN,
        OUT,
        VALUE
    }

    static class BezierMousePointDescriptor {
        int draggedIndex = -1;
        PointType draggedPointType;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + draggedIndex;
            result = prime * result + ((draggedPointType == null) ? 0 : draggedPointType.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            BezierMousePointDescriptor other = (BezierMousePointDescriptor) obj;
            if (draggedIndex != other.draggedIndex)
                return false;
            if (draggedPointType != other.draggedPointType)
                return false;
            return true;
        }

    }

    @Override
    protected List<MenuItem> contextMenuForElementIndex(int elementIndex, CurveEditorMouseRequest request) {
        return List.of();
    }

}
