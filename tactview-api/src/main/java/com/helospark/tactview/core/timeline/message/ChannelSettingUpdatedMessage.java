package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class ChannelSettingUpdatedMessage implements AffectedModifiedIntervalAware {
    private TimelineInterval channelInterval;

    public ChannelSettingUpdatedMessage(TimelineInterval channelInterval) {
        this.channelInterval = channelInterval;
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(channelInterval);
    }

}
