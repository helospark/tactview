package penner.easing;

//https://github.com/jesusgollonet/processing-penner-easing
public class Expo {

    public static double easeIn(double t, double b, double c, double d) {
        return (t == 0) ? b : c * (double) Math.pow(2, 10 * (t / d - 1)) + b;
    }

    public static double easeOut(double t, double b, double c, double d) {
        return (t == d) ? b + c : c * (-(double) Math.pow(2, -10 * t / d) + 1) + b;
    }

    public static double easeInOut(double t, double b, double c, double d) {
        if (t == 0)
            return b;
        if (t == d)
            return b + c;
        if ((t /= d / 2) < 1)
            return c / 2 * (double) Math.pow(2, 10 * (t - 1)) + b;
        return c / 2 * (-(double) Math.pow(2, -10 * --t) + 2) + b;
    }

}
