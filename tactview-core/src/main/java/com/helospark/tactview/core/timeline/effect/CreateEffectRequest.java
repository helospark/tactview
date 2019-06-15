package com.helospark.tactview.core.timeline.effect;

import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class CreateEffectRequest {
    private TimelinePosition position;
    private String effectId;
    private TimelineClipType timelineClipType;
    private TimelineInterval clipInterval;

    public CreateEffectRequest(TimelinePosition position, String effectId, TimelineClipType timelineClipType, TimelineInterval clipInterval) {
        this.position = position;
        this.effectId = effectId;
        this.timelineClipType = timelineClipType;
        this.clipInterval = clipInterval;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public String getEffectId() {
        return effectId;
    }

    public TimelineClipType getTimelineClipType() {
        return timelineClipType;
    }

    public TimelineInterval getClipInterval() {
        return clipInterval;
    }

    @Override
    public String toString() {
        return "CreateEffectRequest [position=" + position + ", effectId=" + effectId + ", timelineClipType=" + timelineClipType + ", clipInterval=" + clipInterval + "]";
    }

}
