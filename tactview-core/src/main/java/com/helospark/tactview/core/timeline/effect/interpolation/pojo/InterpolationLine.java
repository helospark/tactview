package com.helospark.tactview.core.timeline.effect.interpolation.pojo;

public class InterpolationLine {
    public Point start;
    public Point end;

    public InterpolationLine(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public InterpolationLine deepClone() {
        return new InterpolationLine(start.deepClone(), end.deepClone());
    }

    public double length() {
        return end.distanceFrom(start);
    }

}
