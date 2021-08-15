package com.helospark.tactview.core.timeline.effect.interpolation.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Point {
    public double x;
    public double y;

    public Point(@JsonProperty("x") double x, @JsonProperty("y") double y) {
        this.x = x;
        this.y = y;
    }

    public double distanceFrom(Point other) {
        return distanceFrom(other.x, other.y);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
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

    public Point multiply(double otherX, double otherY) {
        return new Point(x * otherX, y * otherY);
    }

    public Point scalarMultiply(double scale) {
        return new Point(x * scale, y * scale);
    }

    public Point scalarDivide(double scale) {
        return new Point(x / scale, y / scale);
    }

    public Point multiply(Point rhs) {
        return new Point(x * rhs.x, y * rhs.y);
    }

    public Point deepClone() {
        return new Point(x, y);
    }

    public Point add(double x, double y) {
        return new Point(this.x + x, this.y + y);
    }

    public Point subtract(Point other) {
        return new Point(this.x - other.x, this.y - other.y);
    }

    public Point add(Point multiplyVector) {
        return add(multiplyVector.x, multiplyVector.y);
    }

    public Point normalize() {
        double length = length();
        return new Point(x / length, y / length);
    }

    public Point invert() {
        return new Point(-x, -y);
    }

    @Override
    public String toString() {
        return "Point [x=" + x + ", y=" + y + "]";
    }

}
