package com.helospark.tactview.core.timeline;

public class GetPositionParameters {
    private TimelinePosition timelinePosition;
    private double scale;
    private int width;
    private int height;

    public GetPositionParameters(TimelinePosition timelinePosition, double scale, int width, int height) {
        this.timelinePosition = timelinePosition;
        this.scale = scale;
        this.width = width;
        this.height = height;
    }

    public TimelinePosition getTimelinePosition() {
        return timelinePosition;
    }

    public double getScale() {
        return scale;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}