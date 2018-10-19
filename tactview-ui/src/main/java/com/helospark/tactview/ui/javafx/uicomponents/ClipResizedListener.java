package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.message.ClipResizedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.scene.shape.Rectangle;

@Component
public class ClipResizedListener {
    private MessagingService messagingService;
    private TimelineState timelineState;

    public ClipResizedListener(MessagingService messagingService, TimelineState timelineState) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ClipResizedMessage.class, message -> {
            timelineState.findClipById(message.getClipId())
                    .ifPresent(clipGroup -> {
                        TimelineInterval interval = message.getNewInterval();
                        double startPosition = timelineState.secondsToPixels(interval.getStartPosition());
                        double width = timelineState.secondsToPixels(interval.getLength());
                        clipGroup.setTranslateX(startPosition);
                        ((Rectangle) clipGroup.getChildren().get(0)).setWidth(width);
                    });
        });
    }

}
