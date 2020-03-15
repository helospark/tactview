package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.ChannelMovedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;

@Component
public class ChannelMovedListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;

    public ChannelMovedListener(UiMessagingService messagingService, TimelineState timelineState) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void setup() {
        messagingService.register(ChannelMovedMessage.class, message -> moveChannel(message));
    }

    private void moveChannel(ChannelMovedMessage message) {
        timelineState.moveChannel(message.getOriginalIndex(), message.getNewIndex());
    }
}
