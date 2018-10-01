package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

@Component
public class EffectAddedListener {
    private MessagingService messagingService;
    private TimelineState timelineState;
    private EffectPropertyView effectPropertyView;

    public EffectAddedListener(MessagingService messagingService, TimelineState timelineState, EffectPropertyView effectPropertyView) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.effectPropertyView = effectPropertyView;
    }

    @PostConstruct
    public void setUp() {
        messagingService.register(EffectAddedMessage.class, message -> Platform.runLater(() -> addEffectClip(message)));
    }

    private void addEffectClip(EffectAddedMessage message) {
        timelineState.addEffectToClip(message.getClipId(), createEffect(message));
    }

    public Node createEffect(EffectAddedMessage clipAddedMessage) {
        Rectangle rectangle = new Rectangle();
        int width = timelineState.secondsToPixels(clipAddedMessage.getEffect().getInterval().getWidth());
        rectangle.setWidth(width);
        rectangle.setHeight(30);
        rectangle.translateXProperty().set(timelineState.secondsToPixels(clipAddedMessage.getPosition()));
        rectangle.translateYProperty().set(40);
        rectangle.setUserData(clipAddedMessage.getEffectId());

        rectangle.setOnMouseClicked(event -> {
            effectPropertyView.showProperties(clipAddedMessage.getEffectId());
        });

        return rectangle;
    }

}
