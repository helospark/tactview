package penner.easing;

//https://github.com/jesusgollonet/processing-penner-easing
public class Bounce {

    public static double easeIn(double t, double b, double c, double d) {
        return c - easeOut(d - t, 0, c, d) + b;
    }

    public static double easeOut(double t, double b, double c, double d) {
        if ((t /= d) < (1 / 2.75)) {
            return c * (7.5625 * t * t) + b;
        } else if (t < (2 / 2.75)) {
            return c * (7.5625 * (t -= (1.5 / 2.75)) * t + .75) + b;
        } else if (t < (2.5 / 2.75)) {
            return c * (7.5625 * (t -= (2.25 / 2.75)) * t + .9375) + b;
        } else {
            return c * (7.5625 * (t -= (2.625 / 2.75)) * t + .984375) + b;
        }
    }

    public static double easeInOut(double t, double b, double c, double d) {
        if (t < d / 2)
            return easeIn(t * 2, 0, c, d) * .5 + b;
        else
            return easeOut(t * 2 - d, 0, c, d) * .5 + c * .5 + b;
    }

}
