package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.Optional;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

@Component
public class EffectAddedListener {
    private MessagingService messagingService;
    private TimelineState timelineState;

    public EffectAddedListener(MessagingService messagingService, TimelineState timelineState) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void setUp() {
        messagingService.register(EffectAddedMessage.class, message -> addEffectClip(message));
    }

    private void addEffectClip(EffectAddedMessage message) {
        Optional<Group> clip = timelineState.findClipById(message.getClipId());
        if (clip.isPresent()) {
            Pane channel = (Pane) clip.get().getProperties().get("CHANNEL");
            channel.getChildren().add(createEffect(message));
        }
    }

    public Node createEffect(EffectAddedMessage clipAddedMessage) {
        Rectangle rectangle = new Rectangle();
        int width = timelineState.secondsToPixels(clipAddedMessage.getEffect().getInterval().getWidth());
        rectangle.setWidth(width);
        rectangle.setHeight(30);
        rectangle.translateXProperty().set(timelineState.secondsToPixels(clipAddedMessage.getPosition()));
        rectangle.translateYProperty().set(40);
        rectangle.setUserData(clipAddedMessage.getEffectId());

        return rectangle;
    }

}
