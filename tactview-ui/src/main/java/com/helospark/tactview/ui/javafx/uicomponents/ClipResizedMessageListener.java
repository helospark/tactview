package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.Optional;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.message.ClipResizedMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.util.SpecialPointLineDrawer;

@Component
public class ClipResizedMessageListener {
    private UiMessagingService messagingService;
    private TimelineState timelineState;
    private SpecialPointLineDrawer specialPointLineDrawer;
    private TimelineManagerAccessor timelineAccessor;

    public ClipResizedMessageListener(UiMessagingService messagingService, TimelineState timelineState, SpecialPointLineDrawer specialPointLineDrawer, TimelineManagerAccessor timelineAccessor) {
        this.messagingService = messagingService;
        this.timelineState = timelineState;
        this.specialPointLineDrawer = specialPointLineDrawer;
        this.timelineAccessor = timelineAccessor;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ClipResizedMessage.class, message -> {
            move(message);
        });
    }

    private void move(ClipResizedMessage message) {
        if (message.getSpecialPointUsed().isPresent() && message.isMoreResizeExpected()) {
            drawSpecialPositionLine(message);
        } else {
            timelineState.disableSpecialPointLineProperties();
        }
    }

    private void drawSpecialPositionLine(ClipResizedMessage message) {
        ClosesIntervalChannel specialPosition = message.getSpecialPointUsed().get();
        Optional<String> channelId = timelineAccessor.findChannelForClipId(message.getClipId()).map(a -> a.getId());
        if (channelId.isPresent()) {
            specialPointLineDrawer.drawSpecialPositionLineForClip(specialPosition, channelId.get());
        }
    }

}
