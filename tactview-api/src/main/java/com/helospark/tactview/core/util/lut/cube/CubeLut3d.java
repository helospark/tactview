package com.helospark.tactview.core.util.lut.cube;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

public class CubeLut3d extends AbstractCubeLut {
    LutColor[][][] values;

    @Generated("SparkTools")
    private CubeLut3d(Builder builder) {
        this.title = builder.title;
        this.lowerBound = builder.lowerBound;
        this.upperBound = builder.upperBound;
        this.size = builder.size;
        this.values = builder.values;
    }

    @Override
    public Color apply(Color color) {
        float redFloat = calculateFloatArrayIndex(color.red, 0);
        float greenFloat = calculateFloatArrayIndex(color.green, 1);
        float blueFloat = calculateFloatArrayIndex(color.blue, 2);

        LutColor result = tetrahedralInterpolation(new float[]{blueFloat, greenFloat, redFloat});

        return Color.of(result.r, result.g, result.b);
    }

    public LutColor[][][] getRawCubeValues() {
        return values;
    }

    /**
     * Tetrahedral interpolation. Based on code found in Truelight Software Library paper.
     * @see http://www.filmlight.ltd.uk/pdf/whitepapers/FL-TL-TN-0057-SoftwareLib.pdf
     */
    LutColor tetrahedralInterpolation(float[] floatIndices) {
        int[] prev = {(int) floatIndices[0], (int) floatIndices[1], (int) floatIndices[2]};
        int[] next = {roundUpAndClamp(floatIndices[0], size - 1), roundUpAndClamp(floatIndices[1], size - 1), roundUpAndClamp(floatIndices[2], size - 1)};

        LutColor d = new LutColor(floatIndices[0] - prev[0], floatIndices[1] - prev[1], floatIndices[2] - prev[2]);
        LutColor c000 = values[prev[0]][prev[1]][prev[2]];
        LutColor c111 = values[next[0]][next[1]][next[2]];
        LutColor c = new LutColor(0, 0, 0);
        if (d.r > d.g) {
            if (d.g > d.b) {
                LutColor c100 = values[next[0]][prev[1]][prev[2]];
                LutColor c110 = values[next[0]][next[1]][prev[2]];
                c.r = (1 - d.r) * c000.r + (d.r - d.g) * c100.r + (d.g - d.b) * c110.r + (d.b) * c111.r;
                c.g = (1 - d.r) * c000.g + (d.r - d.g) * c100.g + (d.g - d.b) * c110.g + (d.b) * c111.g;
                c.b = (1 - d.r) * c000.b + (d.r - d.g) * c100.b + (d.g - d.b) * c110.b + (d.b) * c111.b;
            } else if (d.r > d.b) {
                LutColor c100 = values[next[0]][prev[1]][prev[2]];
                LutColor c101 = values[next[0]][prev[1]][next[2]];
                c.r = (1 - d.r) * c000.r + (d.r - d.b) * c100.r + (d.b - d.g) * c101.r + (d.g) * c111.r;
                c.g = (1 - d.r) * c000.g + (d.r - d.b) * c100.g + (d.b - d.g) * c101.g + (d.g) * c111.g;
                c.b = (1 - d.r) * c000.b + (d.r - d.b) * c100.b + (d.b - d.g) * c101.b + (d.g) * c111.b;
            } else {
                LutColor c001 = values[prev[0]][prev[1]][next[2]];
                LutColor c101 = values[next[0]][prev[1]][next[2]];
                c.r = (1 - d.b) * c000.r + (d.b - d.r) * c001.r + (d.r - d.g) * c101.r + (d.g) * c111.r;
                c.g = (1 - d.b) * c000.g + (d.b - d.r) * c001.g + (d.r - d.g) * c101.g + (d.g) * c111.g;
                c.b = (1 - d.b) * c000.b + (d.b - d.r) * c001.b + (d.r - d.g) * c101.b + (d.g) * c111.b;
            }
        } else {
            if (d.b > d.g) {
                LutColor c001 = values[prev[0]][prev[1]][next[2]];
                LutColor c011 = values[prev[0]][next[1]][next[2]];
                c.r = (1 - d.b) * c000.r + (d.b - d.g) * c001.r + (d.g - d.r) * c011.r + (d.r) * c111.r;
                c.g = (1 - d.b) * c000.g + (d.b - d.g) * c001.g + (d.g - d.r) * c011.g + (d.r) * c111.g;
                c.b = (1 - d.b) * c000.b + (d.b - d.g) * c001.b + (d.g - d.r) * c011.b + (d.r) * c111.b;
            } else if (d.b > d.r) {
                LutColor c010 = values[prev[0]][next[1]][prev[2]];
                LutColor c011 = values[prev[0]][next[1]][next[2]];
                c.r = (1 - d.g) * c000.r + (d.g - d.b) * c010.r + (d.b - d.r) * c011.r + (d.r) * c111.r;
                c.g = (1 - d.g) * c000.g + (d.g - d.b) * c010.g + (d.b - d.r) * c011.g + (d.r) * c111.g;
                c.b = (1 - d.g) * c000.b + (d.g - d.b) * c010.b + (d.b - d.r) * c011.b + (d.r) * c111.b;
            } else {
                LutColor c010 = values[prev[0]][next[1]][prev[2]];
                LutColor c110 = values[next[0]][next[1]][prev[2]];
                c.r = (1 - d.g) * c000.r + (d.g - d.r) * c010.r + (d.r - d.b) * c110.r + (d.b) * c111.r;
                c.g = (1 - d.g) * c000.g + (d.g - d.r) * c010.g + (d.r - d.b) * c110.g + (d.b) * c111.g;
                c.b = (1 - d.g) * c000.b + (d.g - d.r) * c010.b + (d.r - d.b) * c110.b + (d.b) * c111.b;
            }
        }
        return (c);
    }

    private int roundUpAndClamp(float value, int max) {
        return Math.min((int) Math.ceil(value), max);
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String title;
        private float[] lowerBound;
        private float[] upperBound;
        private int size;
        private LutColor[][][] values;

        private Builder() {
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withLowerBound(float[] lowerBound) {
            this.lowerBound = lowerBound;
            return this;
        }

        public Builder withUpperBound(float[] upperBound) {
            this.upperBound = upperBound;
            return this;
        }

        public Builder withSize(int size) {
            this.size = size;
            return this;
        }

        public Builder withValues(LutColor[][][] values) {
            this.values = values;
            return this;
        }

        public CubeLut3d build() {
            return new CubeLut3d(this);
        }
    }

}
