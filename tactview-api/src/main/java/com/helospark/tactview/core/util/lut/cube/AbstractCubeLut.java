package com.helospark.tactview.core.util.lut.cube;

import com.helospark.tactview.core.util.lut.AbstractLut;

public abstract class AbstractCubeLut extends AbstractLut {
    String title;
    float[] lowerBound;
    float[] upperBound;
    int size;

    protected float calculateFloatArrayIndex(double colorComponent, int index) {
        float distance = upperBound[index] - lowerBound[index];
        float step = distance / (size - 1);

        float floatColorIndex = (float) ((colorComponent - lowerBound[index]) / step);
        return floatColorIndex;
    }

}
