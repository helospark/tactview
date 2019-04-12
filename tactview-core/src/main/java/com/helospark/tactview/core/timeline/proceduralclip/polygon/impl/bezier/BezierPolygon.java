package com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class BezierPolygon {
    public List<BezierPolygonPoint> points;

    public BezierPolygon(List<BezierPolygonPoint> points) {
        this.points = points;
    }

    public List<BezierPolygonPoint> getPoints() {
        return points;
    }

    public BezierPolygon multiplyPoints(Point point) {
        List<BezierPolygonPoint> result = points.stream()
                .map(a -> new BezierPolygonPoint(a.x * point.x, a.y * point.y, a.type))
                .collect(Collectors.toList());
        return new BezierPolygon(result);
    }

}
