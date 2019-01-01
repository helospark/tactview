package com.helospark.tactview.core.util;

public class MathUtil {

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

}
