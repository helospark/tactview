package com.helospark.tactview.ui.javafx.repository.drag;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class ClipDragInformation {
    private TimelinePosition originalPosition;
    private List<String> clipId;
    private String originalChannelId;
    private double anchorPointX;
    private TimelinePosition lastPosition;
    private TimelineInterval originalInterval;
    private boolean hasMovedWithoutRevert;

    public ClipDragInformation(TimelinePosition originalPosition, List<String> clipId, String originalChannelId, double anchorPointX, TimelineInterval originalInterval) {
        this.originalPosition = originalPosition;
        this.clipId = clipId;
        this.originalChannelId = originalChannelId;
        this.anchorPointX = anchorPointX;
        this.lastPosition = originalPosition;
        this.originalInterval = originalInterval;

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

    public TimelinePosition getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(TimelinePosition lastPosition) {
        this.lastPosition = lastPosition;
    }

    public TimelineInterval getOriginalInterval() {
        return originalInterval;
    }

    public void setHasMovedWithoutRevert(boolean hasMovedWithoutRevert) {
        this.hasMovedWithoutRevert = hasMovedWithoutRevert;
    }

    public boolean getHasMovedWithoutRevert() {
        return hasMovedWithoutRevert;
    }

    public void setOriginalInterval(TimelineInterval originalInterval) {
        this.originalInterval = originalInterval;
        this.originalPosition = originalInterval.getStartPosition();
    }

}
