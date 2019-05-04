package com.helospark.tactview.core.timeline.effect.interpolation.pojo;

import java.util.Objects;

public class ColorWithAlpha extends Color {
    public double alpha;

    public ColorWithAlpha(double red, double green, double blue, double alpha) {
        super(red, green, blue);
        this.alpha = alpha;
    }

    @Override
    public String toString() {
        return "ColorWithAlpha [alpha=" + alpha + "]";
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ColorWithAlpha)) {
            return false;
        }
        ColorWithAlpha castOther = (ColorWithAlpha) other;
        return Objects.equals(red, castOther.red) && Objects.equals(green, castOther.green) && Objects.equals(blue, castOther.blue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue);
    }

}
