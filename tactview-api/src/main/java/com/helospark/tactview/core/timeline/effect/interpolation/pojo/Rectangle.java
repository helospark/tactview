package com.helospark.tactview.core.timeline.effect.interpolation.pojo;

import java.util.List;

public class Rectangle {
    public List<Point> points;

    public Rectangle(List<Point> points) {
        this.points = points;
    }

    public String serializeToString() {
        String result = "";
        for (var point : points) {
            result += point.x + "," + point.y + ";";
        }
        return result;
    }

    public double getLength() {
        return points.get(0).distanceFrom(points.get(1)) +
                points.get(1).distanceFrom(points.get(2)) +
                points.get(2).distanceFrom(points.get(3)) +
                points.get(3).distanceFrom(points.get(0));
    }

    @Override
    public String toString() {
        return serializeToString();
    }

}
