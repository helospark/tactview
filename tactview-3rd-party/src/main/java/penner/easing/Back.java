package penner.easing;

// https://github.com/jesusgollonet/processing-penner-easing
public class Back {

    public static double easeIn(double t, double b, double c, double d) {
        double s = 1.70158;
        return c * (t /= d) * t * ((s + 1) * t - s) + b;
    }

    public static double easeIn(double t, double b, double c, double d, double s) {
        return c * (t /= d) * t * ((s + 1) * t - s) + b;
    }

    public static double easeOut(double t, double b, double c, double d) {
        double s = 1.70158;
        return c * ((t = t / d - 1) * t * ((s + 1) * t + s) + 1) + b;
    }

    public static double easeOut(double t, double b, double c, double d, double s) {
        return c * ((t = t / d - 1) * t * ((s + 1) * t + s) + 1) + b;
    }

    public static double easeInOut(double t, double b, double c, double d) {
        double s = 1.70158;
        if ((t /= d / 2) < 1)
            return c / 2 * (t * t * (((s *= (1.525)) + 1) * t - s)) + b;
        return c / 2 * ((t -= 2) * t * (((s *= (1.525)) + 1) * t + s) + 2) + b;
    }

    public static double easeInOut(double t, double b, double c, double d, double s) {
        if ((t /= d / 2) < 1)
            return c / 2 * (t * t * (((s *= (1.525)) + 1) * t - s)) + b;
        return c / 2 * ((t -= 2) * t * (((s *= (1.525)) + 1) * t + s) + 2) + b;
    }

}
