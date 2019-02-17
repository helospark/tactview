package penner.easing;

//https://github.com/jesusgollonet/processing-penner-easing
public class Quad {

    public static double easeIn(double t, double b, double c, double d) {
        return c * (t /= d) * t + b;
    }

    public static double easeOut(double t, double b, double c, double d) {
        return -c * (t /= d) * (t - 2) + b;
    }

    public static double easeInOut(double t, double b, double c, double d) {
        if ((t /= d / 2) < 1)
            return c / 2 * t * t + b;
        return -c / 2 * ((--t) * (t - 2) - 1) + b;
    }

}
