package com.helospark.tactview.core.timeline.message;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class ClipMovedMessage {
    private String clipId;
    private TimelinePosition newPosition;
    private String channelId;

    public ClipMovedMessage(String clipId, TimelinePosition newPosition, String newChannelId) {
        this.clipId = clipId;
        this.newPosition = newPosition;
        this.channelId = newChannelId;
    }

    public String getClipId() {
        return clipId;
    }

    public void setClipId(String clipId) {
        this.clipId = clipId;
    }

    public TimelinePosition getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(TimelinePosition newPosition) {
        this.newPosition = newPosition;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

}
