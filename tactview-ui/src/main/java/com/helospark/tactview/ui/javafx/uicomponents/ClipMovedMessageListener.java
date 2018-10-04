package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.ClipMovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

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
        messagingService.register(ClipMovedMessage.class, message -> {
            timelineState.findClipById(message.getClipId())
                    .ifPresent(group -> group.setTranslateX(timelineState.secondsToPixels(message.getNewPosition())));
        });
    }

}
