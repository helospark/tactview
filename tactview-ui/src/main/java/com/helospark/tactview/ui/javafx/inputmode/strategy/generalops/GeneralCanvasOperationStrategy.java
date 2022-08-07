package com.helospark.tactview.ui.javafx.inputmode.strategy.generalops;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.TimelineRenderResult.RegularRectangle;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.effect.scale.ScaleEffect;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.CanvasStateHolder;
import com.helospark.tactview.ui.javafx.DisplayUpdateRequestMessage;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddEffectCommand;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.commands.impl.CompositeCommand;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.uicomponents.DefaultCanvasTranslateSetter;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;

@Component
public class GeneralCanvasOperationStrategy {
    private static final int CLOSE_THRESHOLD = 5;
    private UiCommandInterpreterService commandInterpreterService;
    private EffectParametersRepository effectParametersRepository;
    private GlobalTimelinePositionHolder uiTimelineManager;
    private UiProjectRepository projectRepository;
    private TimelineManagerAccessor timelineManagerAccessor;
    private CanvasStateHolder canvasStateHolder;
    private MessagingService messagingService;
    private DefaultCanvasTranslateSetter defaultCanvasTranslateSetter;

    private DragData dragData;
    private Point dragStartPoint = null;
    private Point dragStartPointAbsoluteCanvasPos = null;

    public GeneralCanvasOperationStrategy(UiCommandInterpreterService commandInterpreterService, EffectParametersRepository effectParametersRepository, GlobalTimelinePositionHolder uiTimelineManager,
            UiProjectRepository projectRepository, TimelineManagerAccessor timelineManagerAccessor, CanvasStateHolder canvasStateHolder,
            MessagingService messagingService, DefaultCanvasTranslateSetter defaultCanvasTranslateSetter) {
        this.commandInterpreterService = commandInterpreterService;
        this.effectParametersRepository = effectParametersRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.projectRepository = projectRepository;
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.canvasStateHolder = canvasStateHolder;
        this.messagingService = messagingService;
        this.defaultCanvasTranslateSetter = defaultCanvasTranslateSetter;
    }

    public void onMouseDownEvent(GeneralCanvasOperationsMouseRequest input) {
        dragData = findDragData(input);
        dragStartPoint = new Point(input.x, input.y);
        dragStartPointAbsoluteCanvasPos = new Point(input.canvasRelativeX, input.canvasRelativeY);
    }

    public void onMouseMovedEvent(GeneralCanvasOperationsMouseRequest input) {
        if (dragData == null) {
            DragData moveOverDataData = findDragData(input);
            if (moveOverDataData != null) {
                input.canvas.setCursor(moveOverDataData.dragPointType.cursor);
            } else {
                input.canvas.setCursor(Cursor.DEFAULT);
            }
        } else {
            input.canvas.setCursor(dragData.dragPointType.cursor);
        }
    }

    public void onMouseUpEvent(GeneralCanvasOperationsMouseRequest input) {
        input.canvas.setCursor(Cursor.DEFAULT);
        if (input.mouseEvent.getButton().equals(MouseButton.MIDDLE)
                && input.mouseEvent.getClickCount() > 0
                && input.mouseEvent.isStillSincePress()) {
            defaultCanvasTranslateSetter.setDefaultCanvasTranslate(canvasStateHolder, projectRepository.getPreviewWidth(), projectRepository.getPreviewHeight());
        }
        dragData = null;
    }

    public void onMouseDraggedEvent(GeneralCanvasOperationsMouseRequest input) {
        if (input.mouseEvent.getButton().equals(MouseButton.MIDDLE)) {
            Point relativeMoveNormalized = new Point(input.canvasRelativeX, input.canvasRelativeY).subtract(dragStartPointAbsoluteCanvasPos);

            if (Math.abs(relativeMoveNormalized.distanceFrom(0.0, 0.0)) >= 1.0) {
                canvasStateHolder.increaseTranslateX(relativeMoveNormalized.x);
                canvasStateHolder.increaseTranslateY(relativeMoveNormalized.y);
                messagingService.sendMessage(new DisplayUpdateRequestMessage(false));
            }

            dragStartPointAbsoluteCanvasPos = new Point(input.canvasRelativeX, input.canvasRelativeY);
        } else if (dragData != null) {
            Point relativeMoveNormalized = new Point(input.x, input.y).subtract(dragStartPoint);

            Point relativeScale = relativeMoveNormalized.multiply(projectRepository.getPreviewWidth() / dragData.boundingBox.getWidth(),
                    projectRepository.getPreviewHeight() / dragData.boundingBox.getHeight());

            Point relativeMove = relativeMoveNormalized.multiply(projectRepository.getPreviewWidth(), projectRepository.getPreviewHeight())
                    .scalarDivide(projectRepository.getScaleFactor());
            Point newPosition = dragData.originalClipPosition.add(relativeMove);

            if (dragData.dragPointType.equals(DragPointType.CENTER)) {
                commandInterpreterService.sendWithResult(createKeyframeCommandWithValue(newPosition, "translate", dragData.draggedClip));
            } else if (dragData.dragPointType.equals(DragPointType.BOTTOM_RIGHT)) {
                String scaleEffectId = getOrAddScaleEffect(dragData.draggedClip);

                double newWidth = (1.0 + relativeScale.x) * dragData.originalScale.x;
                double newHeight = (1.0 + relativeScale.y) * dragData.originalScale.y;

                AddKeyframeForPropertyCommand widthScaleCommand = createKeyframeCommandWithValue(newWidth, "width scale", scaleEffectId);
                AddKeyframeForPropertyCommand heigthScaleCommand = createKeyframeCommandWithValue(newHeight, "height scale", scaleEffectId);

                commandInterpreterService.sendWithResult(new CompositeCommand(widthScaleCommand, heigthScaleCommand));
            } else if (dragData.dragPointType.equals(DragPointType.BOTTOM_LEFT)) {
                String scaleEffectId = getOrAddScaleEffect(dragData.draggedClip);

                double newWidth = (1.0 - relativeScale.x) * dragData.originalScale.x;
                double newHeight = (1.0 + relativeScale.y) * dragData.originalScale.y;

                AddKeyframeForPropertyCommand xMoveCommand = createKeyframeCommandWithValue(new Point(newPosition.x, dragData.originalClipPosition.y), "translate", dragData.draggedClip);
                AddKeyframeForPropertyCommand widthScaleCommand = createKeyframeCommandWithValue(newWidth, "width scale", scaleEffectId);
                AddKeyframeForPropertyCommand heigthScaleCommand = createKeyframeCommandWithValue(newHeight, "height scale", scaleEffectId);

                commandInterpreterService.sendWithResult(new CompositeCommand(xMoveCommand, widthScaleCommand, heigthScaleCommand));
            } else if (dragData.dragPointType.equals(DragPointType.TOP_RIGHT)) {
                String scaleEffectId = getOrAddScaleEffect(dragData.draggedClip);

                double newWidth = (1.0 + relativeScale.x) * dragData.originalScale.x;
                double newHeight = (1.0 - relativeScale.y) * dragData.originalScale.y;

                AddKeyframeForPropertyCommand xMoveCommand = createKeyframeCommandWithValue(new Point(dragData.originalClipPosition.x, newPosition.y), "translate", dragData.draggedClip);
                AddKeyframeForPropertyCommand widthScaleCommand = createKeyframeCommandWithValue(newWidth, "width scale", scaleEffectId);
                AddKeyframeForPropertyCommand heigthScaleCommand = createKeyframeCommandWithValue(newHeight, "height scale", scaleEffectId);

                commandInterpreterService.sendWithResult(new CompositeCommand(xMoveCommand, widthScaleCommand, heigthScaleCommand));
            } else if (dragData.dragPointType.equals(DragPointType.TOP_LEFT)) {
                String scaleEffectId = getOrAddScaleEffect(dragData.draggedClip);

                double newWidth = (1.0 - relativeScale.x) * dragData.originalScale.x;
                double newHeight = (1.0 - relativeScale.y) * dragData.originalScale.y;

                AddKeyframeForPropertyCommand xMoveCommand = createKeyframeCommandWithValue(newPosition, "translate", dragData.draggedClip);
                AddKeyframeForPropertyCommand widthScaleCommand = createKeyframeCommandWithValue(newWidth, "width scale", scaleEffectId);
                AddKeyframeForPropertyCommand heigthScaleCommand = createKeyframeCommandWithValue(newHeight, "height scale", scaleEffectId);

                commandInterpreterService.sendWithResult(new CompositeCommand(xMoveCommand, widthScaleCommand, heigthScaleCommand));
            } else if (dragData.dragPointType.equals(DragPointType.LEFT)) {
                String scaleEffectId = getOrAddScaleEffect(dragData.draggedClip);

                double newWidth = (1.0 - relativeScale.x) * dragData.originalScale.x;

                AddKeyframeForPropertyCommand xMoveCommand = createKeyframeCommandWithValue(new Point(newPosition.x, dragData.originalClipPosition.y), "translate", dragData.draggedClip);
                AddKeyframeForPropertyCommand widthScaleCommand = createKeyframeCommandWithValue(newWidth, "width scale", scaleEffectId);

                commandInterpreterService.sendWithResult(new CompositeCommand(xMoveCommand, widthScaleCommand));
            } else if (dragData.dragPointType.equals(DragPointType.RIGHT)) {
                String scaleEffectId = getOrAddScaleEffect(dragData.draggedClip);

                double newWidth = (1.0 + relativeScale.x) * dragData.originalScale.x;

                AddKeyframeForPropertyCommand widthScaleCommand = createKeyframeCommandWithValue(newWidth, "width scale", scaleEffectId);

                commandInterpreterService.sendWithResult(widthScaleCommand);
            } else if (dragData.dragPointType.equals(DragPointType.TOP)) {
                String scaleEffectId = getOrAddScaleEffect(dragData.draggedClip);

                double newHeight = (1.0 - relativeScale.y) * dragData.originalScale.y;

                AddKeyframeForPropertyCommand xMoveCommand = createKeyframeCommandWithValue(new Point(dragData.originalClipPosition.x, newPosition.y), "translate", dragData.draggedClip);
                AddKeyframeForPropertyCommand heigthScaleCommand = createKeyframeCommandWithValue(newHeight, "height scale", scaleEffectId);

                commandInterpreterService.sendWithResult(new CompositeCommand(xMoveCommand, heigthScaleCommand));
            } else if (dragData.dragPointType.equals(DragPointType.BOTTOM)) {
                String scaleEffectId = getOrAddScaleEffect(dragData.draggedClip);

                double newHeight = (1.0 + relativeScale.y) * dragData.originalScale.y;

                AddKeyframeForPropertyCommand heigthScaleCommand = createKeyframeCommandWithValue(newHeight, "height scale", scaleEffectId);

                commandInterpreterService.sendWithResult(heigthScaleCommand);
            }
        }
    }

    private AddKeyframeForPropertyCommand createKeyframeCommandWithValue(Object newPosition, String keyframeableValueId, String clipOrEffectId) {
        ValueProviderDescriptor translateElement = effectParametersRepository.findDescriptorForLabelAndClipId(clipOrEffectId, keyframeableValueId).get();
        String elementId = translateElement.getKeyframeableEffect().getId();
        KeyframeAddedRequest request = KeyframeAddedRequest.builder()
                .withDescriptorId(elementId)
                .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
                .withRevertable(true)
                .withValue(newPosition)
                .build();
        return new AddKeyframeForPropertyCommand(effectParametersRepository, request);
    }

    private String getOrAddScaleEffect(String draggedClip) {
        ScaleEffect optionalScale = findOptionalScale(draggedClip);
        if (optionalScale != null) {
            return optionalScale.getId();
        }

        AddEffectCommand addEffectCommand = new AddEffectCommand(draggedClip, "scale", TimelinePosition.ofZero(), timelineManagerAccessor);

        return commandInterpreterService.sendWithResult(addEffectCommand).join().getAddedEffectId();
    }

    public void onMouseExited(Canvas canvas) {
        canvas.setCursor(Cursor.DEFAULT);
        dragData = null;
    }

    private DragData findDragData(GeneralCanvasOperationsMouseRequest input) {
        String draggedClip = null;
        DragPointType dragPointType = null;
        for (var entry : input.rectangles.entrySet()) {
            RegularRectangle rectangle = entry.getValue();
            if (isPointClose(input.unscaledX, input.unscaledY, rectangle.getX(), rectangle.getY())) {
                draggedClip = entry.getKey();
                dragPointType = DragPointType.TOP_LEFT;
                break;
            }
            if (isPointClose(input.unscaledX, input.unscaledY, rectangle.getX() + rectangle.getWidth(), rectangle.getY())) {
                draggedClip = entry.getKey();
                dragPointType = DragPointType.TOP_RIGHT;
                break;
            }
            if (isPointClose(input.unscaledX, input.unscaledY, rectangle.getX(), rectangle.getY() + rectangle.getHeight())) {
                draggedClip = entry.getKey();
                dragPointType = DragPointType.BOTTOM_LEFT;
                break;
            }
            if (isPointClose(input.unscaledX, input.unscaledY, rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight())) {
                draggedClip = entry.getKey();
                dragPointType = DragPointType.BOTTOM_RIGHT;
                break;
            }
            if (Math.abs(input.unscaledX - rectangle.getX()) < CLOSE_THRESHOLD && isYWithinRectangleRange(input.unscaledY, rectangle)) {
                draggedClip = entry.getKey();
                dragPointType = DragPointType.LEFT;
                break;
            }
            if (Math.abs(input.unscaledX - (rectangle.getX() + rectangle.getWidth())) < CLOSE_THRESHOLD && isYWithinRectangleRange(input.unscaledY, rectangle)) {
                draggedClip = entry.getKey();
                dragPointType = DragPointType.RIGHT;
                break;
            }
            if (Math.abs(input.unscaledY - (rectangle.getY() + rectangle.getHeight())) < CLOSE_THRESHOLD && isXWithinRectangleRange(input.unscaledX, rectangle)) {
                draggedClip = entry.getKey();
                dragPointType = DragPointType.BOTTOM;
                break;
            }
            if (Math.abs(input.unscaledY - rectangle.getY()) < CLOSE_THRESHOLD && isXWithinRectangleRange(input.unscaledX, rectangle)) {
                draggedClip = entry.getKey();
                dragPointType = DragPointType.TOP;
                break;
            }
            if (isPointInRectangle(input.unscaledX, input.unscaledY, rectangle)) {
                draggedClip = entry.getKey();
                dragPointType = DragPointType.CENTER;
                break;
            }
        }
        if (draggedClip != null && dragPointType != null) {
            ValueProviderDescriptor translateElement = effectParametersRepository.findDescriptorForLabelAndClipId(draggedClip, "translate").get();
            Point originalPosition = ((PointProvider) translateElement.getKeyframeableEffect()).getValueWithoutScriptAt(uiTimelineManager.getCurrentPosition());

            Point lastScale = new Point(1.0, 1.0);
            ScaleEffect scaleEffect = findOptionalScale(draggedClip);
            if (scaleEffect != null) {
                double xScale = (double) effectParametersRepository.findDescriptorForLabelAndClipId(scaleEffect.getId(), "width scale").get().getKeyframeableEffect()
                        .getValueWithoutScriptAt(uiTimelineManager.getCurrentPosition());
                double yScale = (double) effectParametersRepository.findDescriptorForLabelAndClipId(scaleEffect.getId(), "height scale").get().getKeyframeableEffect()
                        .getValueWithoutScriptAt(uiTimelineManager.getCurrentPosition());
                lastScale = new Point(xScale, yScale);
            }

            return new DragData(draggedClip, dragPointType, originalPosition, lastScale, input.rectangles.get(draggedClip));
        } else {
            return null;
        }
    }

    private ScaleEffect findOptionalScale(String draggedClip) {
        List<StatelessEffect> effects = timelineManagerAccessor.findClipById(draggedClip).get().getEffects();

        for (int i = effects.size() - 1; i >= 0; --i) {
            if (effects.get(i) instanceof ScaleEffect) {
                return (ScaleEffect) effects.get(i);
            }
        }
        return null;
    }

    private boolean isPointInRectangle(double unscaledX, double unscaledY, RegularRectangle rectangle) {
        return isXWithinRectangleRange(unscaledX, rectangle) &&
                isYWithinRectangleRange(unscaledY, rectangle);
    }

    private boolean isXWithinRectangleRange(double unscaledX, RegularRectangle rectangle) {
        return unscaledX > rectangle.getX() && unscaledX < rectangle.getX() + rectangle.getWidth();
    }

    private boolean isYWithinRectangleRange(double unscaledY, RegularRectangle rectangle) {
        return unscaledY > rectangle.getY() && unscaledY < rectangle.getY() + rectangle.getHeight();
    }

    private boolean isPointClose(double unscaledX, double unscaledY, double x, double y) {
        return Math.abs(unscaledX - x) < CLOSE_THRESHOLD && Math.abs(unscaledY - y) < CLOSE_THRESHOLD;
    }

    static class DragData {
        String draggedClip;
        DragPointType dragPointType;
        Point originalClipPosition;
        Point originalScale;
        RegularRectangle boundingBox;

        public DragData(String draggedClip, DragPointType dragPointType, Point originalClipPosition, Point originalScale, RegularRectangle boundingBox) {
            this.draggedClip = draggedClip;
            this.dragPointType = dragPointType;
            this.originalClipPosition = originalClipPosition;
            this.originalScale = originalScale;
            this.boundingBox = boundingBox;
        }

    }

    static enum DragPointType {
        CENTER(Cursor.MOVE),
        TOP_LEFT(Cursor.NW_RESIZE),
        TOP_RIGHT(Cursor.NE_RESIZE),
        BOTTOM_LEFT(Cursor.SW_RESIZE),
        BOTTOM_RIGHT(Cursor.SE_RESIZE),
        TOP(Cursor.N_RESIZE),
        BOTTOM(Cursor.S_RESIZE),
        LEFT(Cursor.W_RESIZE),
        RIGHT(Cursor.E_RESIZE);

        Cursor cursor;

        private DragPointType(Cursor cursor) {
            this.cursor = cursor;
        }

    }
}
