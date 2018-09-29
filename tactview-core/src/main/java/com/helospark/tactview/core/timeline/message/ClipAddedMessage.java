package com.helospark.tactview.core.timeline.message;

import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class ClipAddedMessage {
    private String clipId;
    private String channelId;
    private TimelinePosition position;
    private TimelineClip clip;

    public ClipAddedMessage(String clipId, String channelId, TimelinePosition position, TimelineClip clip) {
        this.clipId = clipId;
        this.channelId = channelId;
        this.position = position;
        this.clip = clip;
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

}
