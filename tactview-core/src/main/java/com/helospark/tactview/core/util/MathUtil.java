package com.helospark.tactview.core.util;

import com.google.common.math.DoubleMath;

public class MathUtil {
    private static final double EPSILON = 0.000001;

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static double linearInterpolate(double s, double e, double t) {
        return s + (e - s) * t;
    }

    public static double bilinearInterpolate(int c00, int c10, int c01, int c11, double tx, double ty) {
        return linearInterpolate(linearInterpolate(c00, c10, tx), linearInterpolate(c01, c11, tx), ty);
    }

    public static double min(double... elements) {
        double result = elements[0];

        for (var element : elements) {
            result = Math.min(result, element);
        }

        return result;
    }

    public static boolean fuzzyEquals(double value1, double value2) {
        return DoubleMath.fuzzyEquals(value1, value2, EPSILON);
    }

    public static int clampToInt(double d, int min, int max) {
        if (d < min) {
            return min;
        } else if (d > max) {
            return max;
        } else {
            return (int) d;
        }
    }

}
