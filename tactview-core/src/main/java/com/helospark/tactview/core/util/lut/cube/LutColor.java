package com.helospark.tactview.core.util.lut.cube;

public class LutColor {
    public float r, g, b;

    public LutColor(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public float get(int index) {
        if (index == 0) {
            return r;
        }
        if (index == 1) {
            return g;
        }
        if (index == 2) {
            return b;
        }
        throw new RuntimeException("Invalid index");
    }

}