package com.helospark.tactview.core.util.lut.cube;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

public class CubeLut1d extends AbstractCubeLut {
    protected rgbvec[] values;

    @Generated("SparkTools")
    private CubeLut1d(Builder builder) {
        this.title = builder.title;
        this.lowerBound = builder.lowerBound;
        this.upperBound = builder.upperBound;
        this.size = builder.size;
        this.values = builder.values;
    }

    @Override
    public Color apply(Color color) {
        double red = interpolate(color.red, 0);
        double green = interpolate(color.green, 1);
        double blue = interpolate(color.blue, 2);

        return Color.of(red, green, blue);
    }

    private float interpolate(double color, int index) {
        float floatColorIndex = calculateFloatArrayIndex(color, index);

        if (floatColorIndex < 0) {
            return values[0].get(index);
        } else if (Math.ceil(floatColorIndex) >= values.length) {
            int lastIndex = values.length - 1;
            return values[lastIndex].get(index);
        } else {
            float lowerColor = values[(int) Math.floor(floatColorIndex)].get(index);
            float upperColor = values[(int) Math.ceil(floatColorIndex)].get(index);

            return linearInterpolation(lowerColor, upperColor, floatColorIndex - (int) floatColorIndex);
        }
    }

    private float linearInterpolation(float resultRLower, float upperRLower, float d) {
        return resultRLower * d + upperRLower * (1.0f - d);
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
        private rgbvec[] values;

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

        public Builder withValues(rgbvec[] values) {
            this.values = values;
            return this;
        }

        public CubeLut1d build() {
            return new CubeLut1d(this);
        }
    }
}
