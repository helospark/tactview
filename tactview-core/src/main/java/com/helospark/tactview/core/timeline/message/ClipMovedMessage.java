package com.helospark.tactview.core.timeline.message;

import java.util.Optional;

import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class ClipMovedMessage {
    private String clipId;
    private TimelinePosition newPosition;
    private String channelId;
    private Optional<ClosesIntervalChannel> specialPositionUsed;

    public ClipMovedMessage(String clipId, TimelinePosition newPosition, String newChannelId, Optional<ClosesIntervalChannel> specialPositionUsed) {
        this.clipId = clipId;
        this.newPosition = newPosition;
        this.channelId = newChannelId;
        this.specialPositionUsed = specialPositionUsed;
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

    public Optional<ClosesIntervalChannel> getSpecialPositionUsed() {
        return specialPositionUsed;
    }

    @Override
    public String toString() {
        return "ClipMovedMessage [clipId=" + clipId + ", newPosition=" + newPosition + ", channelId=" + channelId + ", specialPositionUsed=" + specialPositionUsed + "]";
    }

}
