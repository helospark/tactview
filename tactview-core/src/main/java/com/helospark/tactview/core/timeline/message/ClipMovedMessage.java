package com.helospark.tactview.core.timeline.message;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class ClipMovedMessage {
    private String clipId;
    private TimelinePosition newPosition;

    public ClipMovedMessage(String clipId, TimelinePosition newPosition) {
        this.clipId = clipId;
        this.newPosition = newPosition;
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

}
