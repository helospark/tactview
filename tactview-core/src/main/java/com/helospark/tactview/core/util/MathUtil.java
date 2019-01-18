package com.helospark.tactview.core.util;

public class MathUtil {

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

}
