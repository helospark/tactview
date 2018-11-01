package com.helospark.tactview.core.timeline.effect.interpolation.pojo;

public class Color {
    public double red;
    public double green;
    public double blue;

    public Color(double red, double green, double blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color interpolate(Color endColor, double factor) {
        double newR = endColor.red * factor + red * (1.0 - factor);
        double newG = endColor.green * factor + green * (1.0 - factor);
        double newB = endColor.blue * factor + blue * (1.0 - factor);
        return new Color(newR, newG, newB);
    }

}
