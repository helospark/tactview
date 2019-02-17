package penner.easing;

//https://github.com/jesusgollonet/processing-penner-easing
public class Elastic {

    public static double easeIn(double t, double b, double c, double d) {
        if (t == 0)
            return b;
        if ((t /= d) == 1)
            return b + c;
        double p = d * .3;
        double a = c;
        double s = p / 4;
        return -(a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
    }

    public static double easeIn(double t, double b, double c, double d, double a, double p) {
        double s;
        if (t == 0)
            return b;
        if ((t /= d) == 1)
            return b + c;
        if (a < Math.abs(c)) {
            a = c;
            s = p / 4;
        } else {
            s = p / (2 * Math.PI) * Math.asin(c / a);
        }
        return -(a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
    }

    public static double easeOut(double t, double b, double c, double d) {
        if (t == 0)
            return b;
        if ((t /= d) == 1)
            return b + c;
        double p = d * .3;
        double a = c;
        double s = p / 4;
        return (a * Math.pow(2, -10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b);
    }

    public static double easeOut(double t, double b, double c, double d, double a, double p) {
        double s;
        if (t == 0)
            return b;
        if ((t /= d) == 1)
            return b + c;
        if (a < Math.abs(c)) {
            a = c;
            s = p / 4;
        } else {
            s = p / (2 * Math.PI) * Math.asin(c / a);
        }
        return (a * Math.pow(2, -10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b);
    }

    public static double easeInOut(double t, double b, double c, double d) {
        if (t == 0)
            return b;
        if ((t /= d / 2) == 2)
            return b + c;
        double p = d * (.3 * 1.5);
        double a = c;
        double s = p / 4;
        if (t < 1)
            return -.5 * (a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
        return a * Math.pow(2, -10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p) * .5 + c + b;
    }

    public static double easeInOut(double t, double b, double c, double d, double a, double p) {
        double s;
        if (t == 0)
            return b;
        if ((t /= d / 2) == 2)
            return b + c;
        if (a < Math.abs(c)) {
            a = c;
            s = p / 4;
        } else {
            s = p / (2 * Math.PI) * Math.asin(c / a);
        }
        if (t < 1)
            return -.5 * (a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
        return a * Math.pow(2, -10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p) * .5 + c + b;
    }

}
