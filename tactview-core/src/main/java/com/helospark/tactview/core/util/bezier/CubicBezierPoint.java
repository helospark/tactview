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

}
