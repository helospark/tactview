package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.message.EffectResizedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.util.SpecialPointLineDrawer;

import javafx.scene.shape.Rectangle;

@Component
public class EffectResizedListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private SpecialPointLineDrawer specialPointLineDrawer;

    public EffectResizedListener(UiMessagingService messagingService, TimelineState timelineState, SpecialPointLineDrawer specialPointLineDrawer) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.specialPointLineDrawer = specialPointLineDrawer;
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
                        if (message.getSpecialPositionUsed().isPresent()) {
                            specialPointLineDrawer.drawSpecialPointLineForEffect(message.getSpecialPositionUsed().get(), message.getClipId());
                        } else {
                            timelineState.getMoveSpecialPointLineProperties().setEnabledProperty(false);
                        }
                    });
        });
    }

}
