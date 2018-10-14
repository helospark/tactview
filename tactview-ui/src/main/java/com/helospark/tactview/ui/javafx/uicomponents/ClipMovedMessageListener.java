package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.ClipMovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.application.Platform;
import javafx.scene.layout.Pane;

@Component
public class ClipMovedMessageListener {
    private MessagingService messagingService;
    private TimelineState timelineState;

    public ClipMovedMessageListener(MessagingService messagingService, TimelineState timelineState) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ClipMovedMessage.class, message -> Platform.runLater(() -> {
            timelineState.findClipById(message.getClipId())
                    .ifPresent(group -> move(message, group));
        }));
    }

    private void move(ClipMovedMessage message, Pane group) {
        group.setTranslateX(timelineState.secondsToPixels(message.getNewPosition()));
        timelineState.changeChannelFor(group, message.getChannelId());
    }

}
