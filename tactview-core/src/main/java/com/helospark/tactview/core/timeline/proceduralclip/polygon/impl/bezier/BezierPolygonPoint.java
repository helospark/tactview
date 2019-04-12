package com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BezierPolygonPoint {
    public double x, y;

    public SplinePolygonType type;

    public BezierPolygonPoint(@JsonProperty("x") double x,
            @JsonProperty("y") double y,
            @JsonProperty("type") SplinePolygonType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public SplinePolygonType getType() {
        return type;
    }

    public BezierPolygonPoint deepClone() {
        return new BezierPolygonPoint(x, y, type);
    }

}
