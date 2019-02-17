package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.message.ClipResizedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.util.SpecialPointLineDrawer;

import javafx.scene.shape.Rectangle;

@Component
public class ClipResizedListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private SpecialPointLineDrawer specialPointLineDrawer;

    public ClipResizedListener(UiMessagingService messagingService, TimelineState timelineState, SpecialPointLineDrawer specialPointLineDrawer) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.specialPointLineDrawer = specialPointLineDrawer;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ClipResizedMessage.class, message -> {
            timelineState.findClipById(message.getClipId())
                    .ifPresent(clipGroup -> {
                        TimelineInterval interval = message.getNewInterval();
                        double startPosition = timelineState.secondsToPixels(interval.getStartPosition());
                        double width = timelineState.secondsToPixels(interval.getLength());
                        clipGroup.setLayoutX(startPosition);
                        ((Rectangle) clipGroup.getChildren().get(0)).setWidth(width);

                        if (message.getSpecialPointUsed().isPresent() && message.isMoreResizeExpected()) {
                            drawSpecialPositionLine(message);
                        } else {
                            timelineState.getMoveSpecialPointLineProperties().setEnabledProperty(false);
                        }

                    });
        });
    }

    private void drawSpecialPositionLine(ClipResizedMessage message) {
        ClosesIntervalChannel specialPosition = message.getSpecialPointUsed().get();
        String channelId = timelineState.findChannelForClip(message.getClipId())
                .map(channel -> (String) channel.getUserData())
                .get();
        specialPointLineDrawer.drawSpecialPositionLineForClip(specialPosition, channelId);
    }

}
