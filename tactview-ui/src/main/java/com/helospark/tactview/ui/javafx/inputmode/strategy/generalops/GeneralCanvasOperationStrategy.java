package com.helospark.tactview.ui.javafx.inputmode.strategy.generalops;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineRenderResult.RegularRectangle;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;

@Component
public class GeneralCanvasOperationStrategy {
    private UiCommandInterpreterService commandInterpreterService;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager uiTimelineManager;
    private UiProjectRepository projectRepository;

    private DragData dragData;
    private Point lastDragPoint = null;

    public GeneralCanvasOperationStrategy(UiCommandInterpreterService commandInterpreterService, EffectParametersRepository effectParametersRepository, UiTimelineManager uiTimelineManager,
            UiProjectRepository projectRepository) {
        this.commandInterpreterService = commandInterpreterService;
        this.effectParametersRepository = effectParametersRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.projectRepository = projectRepository;
    }

    public void onMouseDownEvent(GeneralCanvasOperationsMouseRequest input) {
        dragData = findDragData(input);
        lastDragPoint = new Point(input.x, input.y);
    }

    public void onMouseMovedEvent(GeneralCanvasOperationsMouseRequest input) {
        if (dragData == null) {
            DragData moveOverDataData = findDragData(input);
            if (moveOverDataData != null) {
                input.canvas.setCursor(moveOverDataData.dragPointType.cursor);
            }
        } else {
            input.canvas.setCursor(dragData.dragPointType.cursor);
        }
    }

    public void onMouseUpEvent(GeneralCanvasOperationsMouseRequest input) {
        input.canvas.setCursor(Cursor.DEFAULT);
        dragData = null;
    }

    public void onMouseDraggedEvent(GeneralCanvasOperationsMouseRequest input) {
        if (dragData != null) {
            if (dragData.dragPointType.equals(DragPointType.CENTER)) {
                Point relativeMove = new Point(input.x, input.y).subtract(lastDragPoint).multiply(projectRepository.getPreviewWidth(), projectRepository.getPreviewWidth())
                        .scalarDivide(projectRepository.getScaleFactor());
                Point newPosition = dragData.originalClipPosition.add(relativeMove);

                Optional<ValueProviderDescriptor> translateElement = effectParametersRepository.findDescriptorForLabelAndClipId(dragData.draggedClip, "translate");
                if (translateElement.isPresent()) {
                    KeyframeAddedRequest request = KeyframeAddedRequest.builder()
                            .withDescriptorId(translateElement.get().getKeyframeableEffect().getId())
                            .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
                            .withRevertable(true)
                            .withValue(newPosition)
                            .build();

                    commandInterpreterService.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, request));
                }
            }
        }
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
            if (isPointInRectangle(input.unscaledX, input.unscaledY, rectangle)) {
                draggedClip = entry.getKey();
                dragPointType = DragPointType.CENTER;
                break;
            }
        }
        if (draggedClip != null && dragPointType != null) {
            ValueProviderDescriptor translateElement = effectParametersRepository.findDescriptorForLabelAndClipId(draggedClip, "translate").get();
            Point originalPosition = ((PointProvider) translateElement.getKeyframeableEffect()).getValueAt(uiTimelineManager.getCurrentPosition());
            return new DragData(draggedClip, dragPointType, originalPosition);
        } else {
            return null;
        }
    }

    private boolean isPointInRectangle(double unscaledX, double unscaledY, RegularRectangle rectangle) {
        return unscaledX > rectangle.getX() && unscaledX < rectangle.getX() + rectangle.getWidth() &&
                unscaledY > rectangle.getY() && unscaledY < rectangle.getY() + rectangle.getHeight();
    }

    private boolean isPointClose(double unscaledX, double unscaledY, double x, double y) {
        return Math.abs(unscaledX - x) < 5 && Math.abs(unscaledY - y) < 5;
    }

    static class DragData {
        String draggedClip;
        DragPointType dragPointType;
        Point originalClipPosition;

        public DragData(String draggedClip, DragPointType dragPointType, Point originalClipPosition) {
            this.draggedClip = draggedClip;
            this.dragPointType = dragPointType;
            this.originalClipPosition = originalClipPosition;
        }

    }

    static enum DragPointType {
        CENTER(Cursor.MOVE),
        TOP_LEFT(Cursor.NW_RESIZE),
        TOP_RIGHT(Cursor.NE_RESIZE),
        BOTTOM_LEFT(Cursor.SW_RESIZE),
        BOTTOM_RIGHT(Cursor.SE_RESIZE);

        Cursor cursor;

        private DragPointType(Cursor cursor) {
            this.cursor = cursor;
        }

    }
}
