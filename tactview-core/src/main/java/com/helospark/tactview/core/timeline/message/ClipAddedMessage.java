package com.helospark.tactview.core.timeline.message;

import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class ClipAddedMessage {
    private String clipId;
    private String channelId;
    private TimelinePosition position;
    private TimelineClip clip;
    private boolean isResizable;

    public ClipAddedMessage(String clipId, String channelId, TimelinePosition position, TimelineClip clip, boolean isResizable) {
        this.clipId = clipId;
        this.channelId = channelId;
        this.position = position;
        this.clip = clip;
        this.isResizable = isResizable;
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

}
