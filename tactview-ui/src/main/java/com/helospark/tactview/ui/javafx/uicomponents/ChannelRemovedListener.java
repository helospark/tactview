package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.ChannelRemovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.application.Platform;

@Component
public class ChannelRemovedListener {
    private MessagingService messagingService;
    private TimelineState timelineState;

    public ChannelRemovedListener(MessagingService messagingService, TimelineState timelineState) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void setup() {
        messagingService.register(ChannelRemovedMessage.class, message -> Platform.runLater(() -> onChannelRemoved(message)));
    }

    private void onChannelRemoved(ChannelRemovedMessage message) {
        timelineState.removeChannel(message.getChannelId());
    }

}
