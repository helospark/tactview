package com.helospark.tactview.core.timeline.effect.interpolation.pojo;

import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private List<Point> points;

    public Polygon(List<Point> points) {
        this.points = points;
    }

    public Polygon() {
        this.points = new ArrayList<>();
    }

    public void copyFrom(Polygon polygon) {
        this.points = new ArrayList<>(polygon.points);
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

    public List<Point> getPoints() {
        return points;
    }

}
