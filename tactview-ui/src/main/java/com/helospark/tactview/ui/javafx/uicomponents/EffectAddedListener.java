package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.effect.EffectContextMenuFactory;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository.DragDirection;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.uicomponents.util.ExtendsClipToMaximizeLengthService;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

@Component
public class EffectAddedListener {
    private static final int DRAG_PIXEL_DISTANCE = 10;
    public static final int EFFECTS_OFFSET = 50;
    public static final int EFFECT_HEIGHT = 30;
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private SelectedNodeRepository selectedNodeRepository;
    private DragRepository dragRepository;
    private NameToIdRepository nameToIdRepository;
    private EffectContextMenuFactory effectContextMenuFactory;
    private ExtendsClipToMaximizeLengthService extendsClipToMaximizeLengthService;

    public EffectAddedListener(UiMessagingService messagingService, TimelineState timelineState, SelectedNodeRepository selectedNodeRepository,
            DragRepository dragRepository,
            EffectDragAdder effectDragAdder, NameToIdRepository nameToIdRepository,
            EffectContextMenuFactory effectContextMenuFactory,
            ExtendsClipToMaximizeLengthService extendsClipToMaximizeLengthService) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.selectedNodeRepository = selectedNodeRepository;
        this.dragRepository = dragRepository;
        this.nameToIdRepository = nameToIdRepository;
        this.effectContextMenuFactory = effectContextMenuFactory;
        this.extendsClipToMaximizeLengthService = extendsClipToMaximizeLengthService;
    }

    @PostConstruct
    public void setUp() {
        messagingService.register(EffectAddedMessage.class, message -> addEffectClip(message));
    }

    private void addEffectClip(EffectAddedMessage message) {
        timelineState.addEffectToClip(message.getClipId(), createEffect(message));
    }

    public Node createEffect(EffectAddedMessage effectAddedMessage) {
        nameToIdRepository.generateAndAddNameForIdIfNotPresent(effectAddedMessage.getEffect().getClass().getSimpleName(), effectAddedMessage.getEffectId());
        Rectangle rectangle = new Rectangle();
        rectangle.setFill(new Color(0 / 255.0, 102 / 255.0, 204 / 255.0, 1.0));
        double width = timelineState.secondsToPixels(effectAddedMessage.getEffect().getInterval().getLength());
        rectangle.setWidth(width);
        rectangle.setHeight(EFFECT_HEIGHT);
        rectangle.layoutXProperty().set(timelineState.secondsToPixels(effectAddedMessage.getPosition()));
        rectangle.layoutYProperty().set(EFFECTS_OFFSET + EFFECT_HEIGHT * effectAddedMessage.getNewEffectChannelId());
        rectangle.setUserData(effectAddedMessage.getEffectId());
        rectangle.getStyleClass().add("timeline-effect");

        //        rectangle.setOnMouseClicked(event -> {
        //            if (event.getClickCount() == 1) {
        //                if (event.isControlDown()) {
        //                    selectedNodeRepository.addSelectedEffect(rectangle);
        //                } else {
        //                    selectedNodeRepository.setOnlySelectedEffect(rectangle);
        //                }
        //            } else {
        //                extendsClipToMaximizeLengthService.extendEffectToClipSize(effectAddedMessage.getClipId(), effectAddedMessage.getEffect());
        //            }
        //        });

        rectangle.setOnDragDetected(event -> {
            ClipboardContent content = new ClipboardContent();
            Dragboard db = rectangle.startDragAndDrop(TransferMode.ANY);
            db.setDragView(ImageReferenceHolder.TRANSPARENT_5x5);
            double currentX = dragRepository.getInitialX();
            EffectDragInformation dragInformation = new EffectDragInformation(effectAddedMessage.getClipId(), effectAddedMessage.getEffectId(), effectAddedMessage.getPosition(),
                    event.getX());
            boolean isResizing = isResizing(rectangle, currentX);
            //            System.out.println("isResizing=" + isResizing + " " + currentX + " " + rectangle);
            if (isResizing) {
                DragDirection dragDirection = isDraggingLeft(rectangle, currentX) ? DragDirection.LEFT : DragDirection.RIGHT;
                System.out.println("DragDirection: " + dragDirection);
                dragRepository.onEffectResized(dragInformation, dragDirection);
                content.putString("effectresized");
            } else {
                dragRepository.onEffectDragged(dragInformation);
                content.putString("effectdrag");
            }
            db.setContent(content);
        });

        rectangle.setOnMousePressed(event -> {
            double currentX = event.getX();

            if (event.isPrimaryButtonDown() && dragRepository.getInitialX() == -1) {
                //                dragRepository.setInitialX(currentX); // this hack is needed because by dragDetect event, cursor could have moved a few pixels
            }

        });

        rectangle.setOnMouseMoved(event -> {
            double currentX = event.getX();
            //            System.out.println("X is : " + currentX + " " + event.isPrimaryButtonDown());
            boolean isResizing = isResizing(rectangle, currentX);
            if (isResizing) {
                rectangle.setCursor(Cursor.H_RESIZE);
            } else {
                rectangle.setCursor(Cursor.HAND);
            }
        });

        rectangle.setOnMouseReleased(event -> {
            dragRepository.clearEffectDrag();
        });

        rectangle.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            ContextMenu contextMenu = effectContextMenuFactory.createContextMenuForEffect(effectAddedMessage.getEffect());
            contextMenu.show(rectangle.getScene().getWindow(), e.getScreenX(), e.getScreenY());
            e.consume();
        });

        return rectangle;
    }

    private boolean isResizing(Rectangle rectangle, double currentX) {
        return (isDraggingLeft(rectangle, currentX) ||
                isDraggingRight(rectangle, currentX));
    }

    private boolean isDraggingLeft(Rectangle rectangle, double currentX) {
        double divider = getDivider(rectangle);
        return currentX < (DRAG_PIXEL_DISTANCE / timelineState.getZoom() / divider);
    }

    private boolean isDraggingRight(Rectangle rectangle, double currentX) {
        double divider = getDivider(rectangle);
        return rectangle.getWidth() - currentX < (DRAG_PIXEL_DISTANCE / timelineState.getZoom() / divider);
    }

    // When the width is small, decrease the resize width
    private double getDivider(Rectangle rectangle) {
        double divider = 1.0;
        if (rectangle.getWidth() * (timelineState.getZoom() / 2.0) < 20.0) {
            divider = 7;
        }
        return divider;
    }

}
