package penner.easing;

//https://github.com/jesusgollonet/processing-penner-easing
public class Sine {

    public static double easeIn(double t, double b, double c, double d) {
        return -c * (double) Math.cos(t / d * (Math.PI / 2)) + c + b;
    }

    public static double easeOut(double t, double b, double c, double d) {
        return c * (double) Math.sin(t / d * (Math.PI / 2)) + b;
    }

    public static double easeInOut(double t, double b, double c, double d) {
        return -c / 2 * ((double) Math.cos(Math.PI * t / d) - 1) + b;
    }

}
