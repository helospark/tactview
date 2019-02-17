package penner.easing;

//https://github.com/jesusgollonet/processing-penner-easing
public class Linear {

    public static double easeNone(double t, double b, double c, double d) {
        return c * t / d + b;
    }

    public static double easeIn(double t, double b, double c, double d) {
        return c * t / d + b;
    }

    public static double easeOut(double t, double b, double c, double d) {
        return c * t / d + b;
    }

    public static double easeInOut(double t, double b, double c, double d) {
        return c * t / d + b;
    }

}
