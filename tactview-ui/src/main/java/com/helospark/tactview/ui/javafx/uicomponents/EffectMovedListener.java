package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.EffectMovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.application.Platform;

@Component
public class EffectMovedListener {
    private TimelineState timelineState;
    private MessagingService messagingService;

    public EffectMovedListener(TimelineState timelineState, MessagingService messagingService) {
        this.timelineState = timelineState;
        this.messagingService = messagingService;
    }

    @PostConstruct
    public void init() {
        this.messagingService.register(EffectMovedMessage.class, message -> Platform.runLater(() -> {
            int position = timelineState.secondsToPixels(message.getNewPosition());
            timelineState.findEffectById(message.getEffectId())
                    .ifPresent(effect -> effect.setLayoutX(position));

            // TODO: rest
            if (!message.getNewClipId().equals(message.getOriginalClipId())) {
            }
        }));
    }

}
