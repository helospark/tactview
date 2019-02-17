package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.message.ClipMovedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.util.SpecialPointLineDrawer;

import javafx.scene.layout.Pane;

@Component
public class ClipMovedMessageListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private SpecialPointLineDrawer specialPointLineDrawer;

    public ClipMovedMessageListener(UiMessagingService messagingService, TimelineState timelineState, SpecialPointLineDrawer specialPointLineDrawer) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.specialPointLineDrawer = specialPointLineDrawer;
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
        if (message.getSpecialPositionUsed().isPresent() && message.isMoreMoveExpected()) {
            drawSpecialPositionLine(message);
        } else {
            timelineState.getMoveSpecialPointLineProperties().setEnabledProperty(false);
        }
        double leftPosition = timelineState.secondsToPixels(message.getAffectedIntervals().get(0).getEndPosition());
        double currentWidth = timelineState.getTimelineWidthProperty().get();
        if (currentWidth - leftPosition < 100) {
            double newPosition = currentWidth + 50;
            if (leftPosition > currentWidth) {
                newPosition = leftPosition + 50;
            }
            timelineState.getTimelineWidthProperty().set(newPosition);
        }
    }

    private void drawSpecialPositionLine(ClipMovedMessage message) {
        ClosesIntervalChannel specialPosition = message.getSpecialPositionUsed().get();
        String channelId = message.getChannelId();
        specialPointLineDrawer.drawSpecialPositionLineForClip(specialPosition, channelId);
    }

}
