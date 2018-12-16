package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.message.ClipMovedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

@Component
public class ClipMovedMessageListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;

    public ClipMovedMessageListener(UiMessagingService messagingService, TimelineState timelineState) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ClipMovedMessage.class, message -> {
            timelineState.findClipById(message.getClipId())
                    .ifPresent(group -> move(message, group));
        });
    }

    private void move(ClipMovedMessage message, Pane group) {
        group.setLayoutX(timelineState.secondsToPixels(message.getNewPosition()));
        timelineState.changeChannelFor(group, message.getChannelId());
        if (message.getSpecialPositionUsed().isPresent()) {
            drawSpecialPositionLine(message);
        } else {
            timelineState.getMoveSpecialPointLineProperties().setEnabledProperty(false);
        }
    }

    private void drawSpecialPositionLine(ClipMovedMessage message) {
        ClosesIntervalChannel specialPosition = message.getSpecialPositionUsed().get();
        HBox specialPositionChannel = timelineState.findChannelById(specialPosition.getChannelId()).orElseThrow();
        HBox currentChannel = timelineState.findChannelById(message.getChannelId()).orElseThrow();

        int lineStartX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());
        int lineEndX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());

        int lineStartY = (int) specialPositionChannel.getLayoutY();
        int lineEndY = (int) (currentChannel.getLayoutY() + currentChannel.getHeight());

        MoveSpecialPointLineProperties properties = timelineState.getMoveSpecialPointLineProperties();
        properties.setStartX(lineStartX);
        properties.setStartY(lineStartY);
        properties.setEndX(lineEndX);
        properties.setEndY(lineEndY);
        properties.setEnabledProperty(true);
    }

}
