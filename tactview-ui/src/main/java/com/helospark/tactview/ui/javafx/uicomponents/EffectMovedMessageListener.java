package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.message.EffectMovedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.util.SpecialPointLineDrawer;

@Component
public class EffectMovedMessageListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private TimelineManagerAccessor timelineManagerAccessor;
    private SpecialPointLineDrawer specialPointLineDrawer;

    public EffectMovedMessageListener(UiMessagingService messagingService, TimelineState timelineState, SpecialPointLineDrawer specialPointLineDrawer,
            TimelineManagerAccessor timelineManagerAccessor) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.specialPointLineDrawer = specialPointLineDrawer;
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    @PostConstruct
    public void init() {
        messagingService.register(EffectMovedMessage.class, message -> {
            move(message);
        });
    }

    private void move(EffectMovedMessage message) {
        if (message.getSpecialPositionUsed().isPresent() && message.isMoreMoveExpected()) {
            drawSpecialPositionLine(message);
        } else {
            timelineState.disableSpecialPointLineProperties();
        }
    }

    private void drawSpecialPositionLine(EffectMovedMessage message) {
        ClosesIntervalChannel specialPosition = message.getSpecialPositionUsed().get();
        String channelId = timelineManagerAccessor.findChannelForClipId(message.getOriginalClipId()).get().getId();
        specialPointLineDrawer.drawSpecialPositionLineForClip(specialPosition, channelId);
    }

}
