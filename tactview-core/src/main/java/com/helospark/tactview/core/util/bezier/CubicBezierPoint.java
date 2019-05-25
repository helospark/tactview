package com.helospark.tactview.core.util.bezier;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class CubicBezierPoint {
    public double value;
    public Point controlPointIn;
    public Point controlPointOut;

    public CubicBezierPoint(@JsonProperty("value") double value, @JsonProperty("controlPointIn") Point controlPointIn, @JsonProperty("controlPointOut") Point controlPointOut) {
        this.value = value;
        this.controlPointIn = controlPointIn;
        this.controlPointOut = controlPointOut;
    }

    public CubicBezierPoint butWithInControlPoint(Point newValue) {
        return new CubicBezierPoint(value, newValue, controlPointOut);
    }

    public CubicBezierPoint butWithOutControlPoint(Point newValue) {
        return new CubicBezierPoint(value, controlPointIn, newValue);
    }
}
