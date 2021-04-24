package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class EffectDragInformation {
    private String clipId;
    private String effectId;
    private TimelinePosition originalPosition;
    private double anchorPointX;

    public EffectDragInformation(String clipId, String effectId, TimelinePosition originalPosition, double anchorPointX) {
        this.clipId = clipId;
        this.effectId = effectId;
        this.originalPosition = originalPosition;
        this.anchorPointX = anchorPointX;
    }

    public String getClipId() {
        return clipId;
    }

    public String getEffectId() {
        return effectId;
    }

    public TimelinePosition getOriginalPosition() {
        return originalPosition;
    }

    public double getAnchorPointX() {
        return anchorPointX;
    }

}
