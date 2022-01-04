package com.helospark.tactview.core.timeline.effect.interpolation.pojo;

import java.util.Objects;

public class Color {
    public double red;
    public double green;
    public double blue;

    public Color(double red, double green, double blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color(Color color) {
        this.red = color.red;
        this.green = color.green;
        this.blue = color.blue;
    }

    public Color interpolate(Color endColor, double factor) {
        double newR = endColor.red * factor + red * (1.0 - factor);
        double newG = endColor.green * factor + green * (1.0 - factor);
        double newB = endColor.blue * factor + blue * (1.0 - factor);
        return Color.of(newR, newG, newB);
    }

    public Color subtractFromComponents(double b) {
        double newred = red - b;
        double newgreen = green - b;
        double newblue = blue - b;
        return Color.of(newred, newgreen, newblue);
    }

    public Color divideComponents(double b) {
        double newred = red / b;
        double newgreen = green / b;
        double newblue = blue / b;
        return Color.of(newred, newgreen, newblue);
    }

    public Color addComponents(double b) {
        double newred = red + b;
        double newgreen = green + b;
        double newblue = blue + b;
        return Color.of(newred, newgreen, newblue);
    }

    public Color clamp(double min, double max) {
        double newred = clampComponent(this.red, min, max);
        double newgreen = clampComponent(this.green, min, max);
        double newblue = clampComponent(this.blue, min, max);
        return Color.of(newred, newgreen, newblue);
    }

    private double clampComponent(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public Color multiplyComponents(double b) {
        double newred = red * b;
        double newgreen = green * b;
        double newblue = blue * b;
        return Color.of(newred, newgreen, newblue);
    }

    public Color multiply(Color shadowsShift) {
        double newred = red * shadowsShift.red;
        double newgreen = green * shadowsShift.green;
        double newblue = blue * shadowsShift.blue;
        return Color.of(newred, newgreen, newblue);
    }

    public Color add(Color shadowsShift) {
        double newred = red + shadowsShift.red;
        double newgreen = green + shadowsShift.green;
        double newblue = blue + shadowsShift.blue;

        return Color.of(newred, newgreen, newblue);
    }

    public static Color of(double newred, double newgreen, double newblue) {
        return new Color(newred, newgreen, newblue);
    }

    //https://github.com/liovch/GPUImage/commit/fcc85db4fdafae1d4e41313c96bb1cac54dc93b4
    public Color rgbToHsl() {
        Color hsl = new Color(0, 0, 0);
        double fmin = Math.min(Math.min(this.red, this.green), this.blue);
        double fmax = Math.max(Math.max(this.red, this.green), this.blue);
        double delta = fmax - fmin;
        hsl.blue = (fmax + fmin) / 2.0;
        if (delta == 0.0) {
            hsl.red = 0.0; // Hue
            hsl.green = 0.0; // Saturation
        } else {
            if (hsl.blue < 0.5)
                hsl.green = delta / (fmax + fmin); // Saturation
            else
                hsl.green = delta / (2.0 - fmax - fmin); // Saturation

            double deltaR = (((fmax - this.red) / 6.0) + (delta / 2.0)) / delta;
            double deltaG = (((fmax - this.green) / 6.0) + (delta / 2.0)) / delta;
            double deltaB = (((fmax - this.blue) / 6.0) + (delta / 2.0)) / delta;

            if (this.red == fmax)
                hsl.red = deltaB - deltaG; // Hue
            else if (this.green == fmax)
                hsl.red = (1.0 / 3.0) + deltaR - deltaB; // Hue
            else if (this.blue == fmax)
                hsl.red = (2.0 / 3.0) + deltaG - deltaR; // Hue

            if (hsl.red < 0.0)
                hsl.red += 1.0; // Hue
            else if (hsl.red > 1.0)
                hsl.red -= 1.0; // Hue
        }
        return hsl;
    }

    //https://github.com/liovch/GPUImage/commit/fcc85db4fdafae1d4e41313c96bb1cac54dc93b4
    public double hueToRgb(double f1, double f2, double hue) {
        if (hue < 0.0)
            hue += 1.0;
        else if (hue > 1.0)
            hue -= 1.0;
        double res;
        if ((6.0 * hue) < 1.0)
            res = f1 + (f2 - f1) * 6.0 * hue;
        else if ((2.0 * hue) < 1.0)
            res = f2;
        else if ((3.0 * hue) < 2.0)
            res = f1 + (f2 - f1) * ((2.0 / 3.0) - hue) * 6.0;
        else
            res = f1;
        return res;
    }

    public Color hsbToRgbColor() {
        int hsbColor = java.awt.Color.HSBtoRGB((float) red, (float) green, (float) blue);
        int blue = (hsbColor >> 0) & 0xFF;
        int green = (hsbColor >> 8) & 0xFF;
        int red = (hsbColor >> 16) & 0xFF;

        return Color.of(red / 255.0, green / 255.0, blue / 255.0);
    }

    public Color rgbToHsbColor() {
        float[] result = java.awt.Color.RGBtoHSB((int) (red * 255), (int) (green * 255), (int) (blue * 255), null);
        return new Color(result[0], result[1], result[2]);
    }

    //https://github.com/liovch/GPUImage/commit/fcc85db4fdafae1d4e41313c96bb1cac54dc93b4
    public Color hslToRgbColor() {
        Color hsl = this;
        Color rgb = new Color(0, 0, 0);
        if (hsl.green == 0.0)
            rgb = new Color(hsl.blue, hsl.blue, hsl.blue);
        else {
            double f2;

            if (hsl.blue < 0.5)
                f2 = hsl.blue * (1.0 + hsl.green);
            else
                f2 = (hsl.blue + hsl.green) - (hsl.green * hsl.blue);

            double f1 = 2.0 * hsl.blue - f2;

            rgb.red = hueToRgb(f1, f2, hsl.red + (1.0 / 3.0));
            rgb.green = hueToRgb(f1, f2, hsl.red);
            rgb.blue = hueToRgb(f1, f2, hsl.red - (1.0 / 3.0));
        }
        return rgb;
    }

    //https://github.com/liovch/GPUImage/commit/fcc85db4fdafae1d4e41313c96bb1cac54dc93b4
    public double getLuminance() {
        Color color = this;
        double fmin = Math.min(Math.min(color.red, color.green), color.blue);
        double fmax = Math.max(Math.max(color.red, color.green), color.blue);
        return (fmax + fmin) / 2.0;
    }

    @Override
    public String toString() {
        return "Color [red=" + red + ", green=" + green + ", blue=" + blue + "]";
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Color)) {
            return false;
        }
        Color castOther = (Color) other;
        return Objects.equals(red, castOther.red) && Objects.equals(green, castOther.green) && Objects.equals(blue, castOther.blue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue);
    }

    public double getRed() {
        return red;
    }

    public double getGreen() {
        return green;
    }

    public double getBlue() {
        return blue;
    }

    public double getHue() {
        return red;
    }

    public double getSaturation() {
        return green;
    }

    public double getLightness() {
        return blue;
    }

}
