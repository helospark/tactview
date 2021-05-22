package com.helospark.tactview.ui.javafx.repository.drag;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class ClipDragInformation {
    private TimelinePosition originalPosition;
    private List<String> clipId;
    private String originalChannelId;
    private double anchorPointX;

    public ClipDragInformation(TimelinePosition originalPosition, List<String> clipId, String originalChannelId, double anchorPointX) {
        this.originalPosition = originalPosition;
        this.clipId = clipId;
        this.originalChannelId = originalChannelId;
        this.anchorPointX = anchorPointX;

    }

    public TimelinePosition getOriginalPosition() {
        return originalPosition;
    }

    public List<String> getClipId() {
        return clipId;
    }

    public String getOriginalChannelId() {
        return originalChannelId;
    }

    public double getAnchorPointX() {
        return anchorPointX;
    }

}
