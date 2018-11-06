package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository.DragDirection;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Rectangle;

@Component
public class EffectAddedListener {
    public static final int EFFECTS_OFFSET = 50;
    public static final int EFFECT_HEIGHT = 30;
    private MessagingService messagingService;
    private TimelineState timelineState;
    private PropertyView effectPropertyView;
    private SelectedNodeRepository selectedNodeRepository;
    private DragRepository dragRepository;
    private EffectDragAdder effectDragAdder;

    public EffectAddedListener(MessagingService messagingService, TimelineState timelineState, PropertyView effectPropertyView, SelectedNodeRepository selectedNodeRepository,
            DragRepository dragRepository,
            EffectDragAdder effectDragAdder) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.effectPropertyView = effectPropertyView;
        this.selectedNodeRepository = selectedNodeRepository;
        this.dragRepository = dragRepository;
        this.effectDragAdder = effectDragAdder;
    }

    @PostConstruct
    public void setUp() {
        messagingService.register(EffectAddedMessage.class, message -> Platform.runLater(() -> addEffectClip(message)));
    }

    private void addEffectClip(EffectAddedMessage message) {
        timelineState.addEffectToClip(message.getClipId(), createEffect(message));
    }

    public Node createEffect(EffectAddedMessage effectAddedMessage) {
        Rectangle rectangle = new Rectangle();
        int width = timelineState.secondsToPixels(effectAddedMessage.getEffect().getInterval().getLength());
        rectangle.setWidth(width);
        rectangle.setHeight(EFFECT_HEIGHT);
        rectangle.layoutXProperty().set(timelineState.secondsToPixels(effectAddedMessage.getPosition()));
        rectangle.layoutYProperty().set(EFFECTS_OFFSET + EFFECT_HEIGHT * effectAddedMessage.getNewEffectChannelId());
        rectangle.setUserData(effectAddedMessage.getEffectId());
        rectangle.getStyleClass().add("timeline-effect");

        rectangle.setOnMouseClicked(event -> {
            selectedNodeRepository.setOnlySelectedEffect(rectangle);
        });

        rectangle.setOnDragDetected(event -> {
            ClipboardContent content = new ClipboardContent();
            Dragboard db = rectangle.startDragAndDrop(TransferMode.ANY);
            double currentX = event.getX();
            EffectDragInformation dragInformation = new EffectDragInformation(rectangle, effectAddedMessage.getClipId(), effectAddedMessage.getEffectId(), effectAddedMessage.getPosition(),
                    event.getX());
            if (isResizing(rectangle, currentX)) {
                dragRepository.onEffectResized(dragInformation, isDraggingLeft(rectangle, currentX) ? DragDirection.LEFT : DragDirection.RIGHT);
                content.putString("effectresized");
            } else {
                dragRepository.onEffectDragged(dragInformation);
                content.putString("effectdrag");
            }
            db.setContent(content);
        });

        rectangle.setOnMouseMoved(event -> {
            double currentX = event.getX();
            if (isResizing(rectangle, currentX)) {
                rectangle.setCursor(Cursor.H_RESIZE);
            } else {
                rectangle.setCursor(Cursor.HAND);
            }
        });

        return rectangle;
    }

    private boolean isResizing(Rectangle rectangle, double currentX) {
        return (isDraggingLeft(rectangle, currentX) ||
                isDraggingRight(rectangle, currentX));
    }

    private boolean isDraggingLeft(Rectangle rectangle, double currentX) {
        return currentX < 15;
    }

    private boolean isDraggingRight(Rectangle rectangle, double currentX) {
        return rectangle.getWidth() - currentX < 15;
    }

}
