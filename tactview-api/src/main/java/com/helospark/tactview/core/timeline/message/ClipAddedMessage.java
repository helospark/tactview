package com.helospark.tactview.core.timeline.message;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;

public class ClipAddedMessage implements AffectedModifiedIntervalAware {
    private String clipId;
    private String channelId;
    private TimelinePosition position;
    private TimelineClip clip;
    private boolean isResizable;
    private TimelineInterval interval;

    public ClipAddedMessage(String clipId, String channelId, TimelinePosition position, TimelineClip clip, boolean isResizable, TimelineInterval interval) {
        this.clipId = clipId;
        this.channelId = channelId;
        this.position = position;
        this.clip = clip;
        this.isResizable = isResizable;
        this.interval = interval;
    }

    public String getClipId() {
        return clipId;
    }

    public String getChannelId() {
        return channelId;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public TimelineClip getClip() {
        return clip;
    }

    public boolean isResizable() {
        return isResizable;
    }

    @Override
    public String toString() {
        return "ClipAddedMessage [clipId=" + clipId + ", channelId=" + channelId + ", position=" + position + ", clip=" + clip + ", isResizable=" + isResizable + "]";
    }

    @Override
    public List<TimelineInterval> getAffectedIntervals() {
        return List.of(interval);
    }

}
