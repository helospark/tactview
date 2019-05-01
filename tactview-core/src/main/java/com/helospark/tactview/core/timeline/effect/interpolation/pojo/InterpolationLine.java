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

    public InterpolationLine multiply(int width, int height) {
        return new InterpolationLine(start.multiply(width, height), end.multiply(width, height));
    }

    @Override
    public String toString() {
        return "InterpolationLine [start=" + start + ", end=" + end + "]";
    }

    public Point getNormalize4dVector() {
        return end.subtract(start).normalize();
    }

}
