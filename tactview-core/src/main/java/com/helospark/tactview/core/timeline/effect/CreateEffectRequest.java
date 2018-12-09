package com.helospark.tactview.core.timeline.effect;

import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class CreateEffectRequest {
    private TimelinePosition position;
    private String effectId;
    private TimelineClipType timelineClipType;

    public CreateEffectRequest(TimelinePosition position, String effectId, TimelineClipType timelineClipType) {
        this.position = position;
        this.effectId = effectId;
        this.timelineClipType = timelineClipType;
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

    @Override
    public String toString() {
        return "CreateEffectRequest [position=" + position + ", effectId=" + effectId + ", timelineClipType=" + timelineClipType + "]";
    }

}
