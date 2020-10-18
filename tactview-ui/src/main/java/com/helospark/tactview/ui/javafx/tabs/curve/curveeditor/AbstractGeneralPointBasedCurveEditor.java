package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.math.BigDecimal;
import java.util.List;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.message.KeyframeRemovedRequest;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.RemoveKeyframeCommand;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

public abstract class AbstractGeneralPointBasedCurveEditor extends AbstractNoOpCurveEditor {
    protected int draggedIndex = -1;
    protected int closeIndex = -1;

    private final UiCommandInterpreterService commandInterpreter;
    private final EffectParametersRepository effectParametersRepository;

    public AbstractGeneralPointBasedCurveEditor(UiCommandInterpreterService commandInterpreter, EffectParametersRepository effectParametersRepository) {
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
    }

    @Override
    public boolean onMouseMoved(CurveEditorMouseRequest mouseEvent) {
        int previous = closeIndex;
        closeIndex = getElementIndex(mouseEvent);
        return draggedIndex != previous;
    }

    @Override
    public boolean onMouseClicked(CurveEditorMouseRequest request) {
        if (request.event.getButton().equals(MouseButton.SECONDARY)) {
            int elementIndex = getElementIndex(request);

            if (elementIndex != -1) {
                List<MenuItem> menuItems = contextMenuForElementIndex(elementIndex, request);
                MenuItem deleteMenu = new MenuItem("Delete");

                deleteMenu.setOnAction(e -> {
                    KeyframePoint elementToRemove = getKeyframePoints((KeyframeSupportingDoubleInterpolator) request.currentDoubleInterpolator).get(elementIndex);

                    KeyframeRemovedRequest keyframeRemoveRequest = KeyframeRemovedRequest.builder()
                            .withDescriptorId(request.currentProvider.getId())
                            .withLocalTimelinePosition(elementToRemove.timelinePosition)
                            .build();

                    commandInterpreter.sendWithResult(new RemoveKeyframeCommand(effectParametersRepository, keyframeRemoveRequest));
                });

                ContextMenu contextMenu = new ContextMenu();
                contextMenu.getItems().addAll(deleteMenu);
                contextMenu.getItems().addAll(menuItems);
                contextMenu.show(request.canvas.getScene().getWindow(), request.event.getScreenX(), request.event.getScreenY());
            }
        }
        if (request.event.getButton().equals(MouseButton.PRIMARY) && request.event.getClickCount() == 2) {
            addNewPoint(request.remappedMousePosition, request);
            return true;
        }
        return false;
    }

    protected void addNewPoint(Point remappedMousePosition, CurveEditorMouseRequest request) {
        // TODO: commandInterpreter
        TimelinePosition offset = effectParametersRepository.findGlobalPositionForValueProvider(request.currentProvider.getId()).get();
        ((KeyframeSupportingDoubleInterpolator) request.currentDoubleInterpolator).valueAdded(new TimelinePosition(remappedMousePosition.x).subtract(offset), remappedMousePosition.y);
    }

    protected abstract List<MenuItem> contextMenuForElementIndex(int elementIndex, CurveEditorMouseRequest request);

    @Override
    public boolean onMouseDown(CurveEditorMouseRequest mouseEvent) {
        draggedIndex = getElementIndex(mouseEvent);
        return false;
    }

    protected int getElementIndex(CurveEditorMouseRequest mouseEvent) {
        List<KeyframePoint> bezierValues = getKeyframePoints((KeyframeSupportingDoubleInterpolator) mouseEvent.currentDoubleInterpolator);
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

        KeyframeSupportingDoubleInterpolator effect = (KeyframeSupportingDoubleInterpolator) mouseEvent.currentDoubleInterpolator;

        List<KeyframePoint> keyframePoints = getKeyframePoints(effect);

        KeyframePoint pointToModify = keyframePoints.get(draggedIndex);

        if (pointToModify != null) {
            TimelinePosition newTime = pointToModify.timelinePosition.add(new BigDecimal(mouseEvent.mouseDelta.x));
            double newValue = mouseEvent.remappedMousePosition.y;
            valueModifiedAt((KeyframeSupportingDoubleInterpolator) mouseEvent.currentDoubleInterpolator, pointToModify.timelinePosition, newTime, newValue);
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
        List<KeyframePoint> keyframes = getKeyframePoints((KeyframeSupportingDoubleInterpolator) drawRequest.currentDoubleInterpolator);

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
        double screenX = ((point.x + request.timeOffset - request.curveViewerOffsetSeconds) * (1.0 / request.secondsPerPixel));
        double screenY = request.height - (((point.y - request.minValue) * request.displayScale));

        return new Point(screenX, screenY);
    }
}
