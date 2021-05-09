package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.message.EffectResizedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.util.SpecialPointLineDrawer;

@Component
public class EffectResizedMessageListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private TimelineManagerAccessor timelineManagerAccessor;
    private SpecialPointLineDrawer specialPointLineDrawer;

    public EffectResizedMessageListener(UiMessagingService messagingService, TimelineState timelineState, SpecialPointLineDrawer specialPointLineDrawer,
            TimelineManagerAccessor timelineManagerAccessor) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.specialPointLineDrawer = specialPointLineDrawer;
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    @PostConstruct
    public void init() {
        messagingService.register(EffectResizedMessage.class, message -> {
            resized(message);
        });
    }

    private void resized(EffectResizedMessage message) {
        if (message.getSpecialPositionUsed().isPresent() && message.isMoreResizeExpected()) {
            drawSpecialPositionLine(message);
        } else {
            timelineState.disableSpecialPointLineProperties();
        }
    }

    private void drawSpecialPositionLine(EffectResizedMessage message) {
        ClosesIntervalChannel specialPosition = message.getSpecialPositionUsed().get();
        String channelId = timelineManagerAccessor.findChannelForClipId(message.getClipId()).get().getId();
        specialPointLineDrawer.drawSpecialPositionLineForClip(specialPosition, channelId);
    }

}
