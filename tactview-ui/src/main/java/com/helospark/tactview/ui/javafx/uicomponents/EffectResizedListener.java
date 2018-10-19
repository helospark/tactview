package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.message.EffectResizedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.scene.shape.Rectangle;

@Component
public class EffectResizedListener {
    private MessagingService messagingService;
    private TimelineState timelineState;

    public EffectResizedListener(MessagingService messagingService, TimelineState timelineState) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void init() {
        messagingService.register(EffectResizedMessage.class, message -> {
            timelineState.findEffectById(message.getEffectId())
                    .ifPresent(effectNode -> {
                        TimelineInterval interval = message.getNewInterval();
                        double startPosition = timelineState.secondsToPixels(interval.getStartPosition());
                        double width = timelineState.secondsToPixels(interval.getLength());

                        effectNode.setLayoutX(startPosition);
                        ((Rectangle) effectNode).setWidth(width);
                    });
        });
    }

}
