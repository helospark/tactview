package com.helospark.tactview.core.timeline.effect.interpolation.pojo;

import java.util.ArrayList;
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

    public static List<Point> deserializePointsFromString(String value) {
        String[] stringPoints = value.split(";");

        List<Point> result = new ArrayList<>();
        for (var entry : stringPoints) {
            if (entry.isEmpty()) {
                continue;
            }
            String[] stringxy = entry.split(",");
            result.add(new Point(Double.parseDouble(stringxy[0]), Double.parseDouble(stringxy[1])));
        }

        return result;
    }

    @Override
    public String toString() {
        return serializeToString();
    }

}
