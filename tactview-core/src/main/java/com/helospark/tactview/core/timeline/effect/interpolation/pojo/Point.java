package com.helospark.tactview.core.timeline.effect.interpolation.pojo;

public class Point {
    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distanceFrom(Point other) {
        return distanceFrom(other.x, other.y);
    }

    public Point center(Point point) {
        double centerX = centerBetween(point.x, x);
        double centerY = centerBetween(point.y, y);
        return new Point(centerX, centerY);
    }

    private double centerBetween(double startX, double endX) {
        return (Math.max(endX, startX) + Math.min(endX, startX)) / 2;
    }

    public double distanceFrom(double x2, double y2) {
        double dx = (x2 - x);
        double dy = (y2 - y);
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Point multiply(int otherX, int otherY) {
        return new Point(x * otherX, y * otherY);
    }

    public Point scalarMultiply(double scale) {
        return new Point(x * scale, y * scale);
    }

}
