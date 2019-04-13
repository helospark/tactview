package com.helospark.tactview.core.util.bezier;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

public class CubicBezierPoint {
    public double value;
    public Point controlPointIn;
    public Point controlPointOut;

    public CubicBezierPoint(double value, Point controlPointIn, Point controlPointOut) {
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
