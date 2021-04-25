package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.message.ClipResizedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.util.SpecialPointLineDrawer;

import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

@Component
public class ClipResizedListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private SpecialPointLineDrawer specialPointLineDrawer;
    private TimelineManagerAccessor timelineManagerAccessor;

    public ClipResizedListener(UiMessagingService messagingService, TimelineState timelineState, SpecialPointLineDrawer specialPointLineDrawer, TimelineManagerAccessor timelineManagerAccessor) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.specialPointLineDrawer = specialPointLineDrawer;
        this.timelineManagerAccessor = timelineManagerAccessor;
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
                            timelineState.disableSpecialPointLineProperties();
                        }

                        TimelineClip clip = timelineManagerAccessor.findClipById(message.getClipId()).get();
                        clip.getEffects().stream()
                                .forEach(effect -> {
                                    Node uiEffect = timelineState.findEffectById(effect.getId()).get();
                                    if (effect.getGlobalInterval().getStartPosition().isGreaterThan(clip.getInterval().getEndPosition())) {
                                        uiEffect.setVisible(false);
                                    } else {
                                        uiEffect.setVisible(true);
                                    }

                                    if (effect.getGlobalInterval().getEndPosition().isGreaterThan(clip.getInterval().getEndPosition())) {
                                        ((Rectangle) uiEffect).setWidth(width - uiEffect.getLayoutX());
                                    } else {
                                        double effectWidth = timelineState.secondsToPixels(effect.getInterval().getLength());
                                        ((Rectangle) uiEffect).setWidth(effectWidth);
                                    }
                                });

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
