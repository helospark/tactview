package com.helospark.tactview.ui.javafx.uicomponents.util;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;

@Component
public class SpecialPointLineDrawer {
    private TimelineState timelineState;
    private TimelineManagerAccessor timelineAccessor;

    public SpecialPointLineDrawer(TimelineState timelineState, TimelineManagerAccessor timelineAccessor) {
        this.timelineState = timelineState;
        this.timelineAccessor = timelineAccessor;
    }

    public void drawSpecialPointLineForEffect(ClosesIntervalChannel specialPosition, String originalClipId) {
        String channelId = timelineAccessor.findChannelForClipId(originalClipId).get().getId();

        timelineState.enableSpecialPointLineProperties(specialPosition.getSpecialPosition(), channelId, channelId);
    }

    public void drawSpecialPositionLineForClip(ClosesIntervalChannel specialPosition, String channelId) {
        timelineState.enableSpecialPointLineProperties(specialPosition.getSpecialPosition(), specialPosition.getChannelId(), channelId);
    }

}
