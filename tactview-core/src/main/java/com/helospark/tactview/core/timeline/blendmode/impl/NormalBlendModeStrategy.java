package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class NormalBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public NormalBlendModeStrategy() {
        super("normal");
    }

    @Override
    public double computeRgbComponent(double topLayer, double bottomLayer) {
        return topLayer;
    }

    @Override
    protected int computeSeparateComponent(int top, int bottom, int alpha) {
        if (alpha == 255) { // Minor performance optimization to avoid double precision math in the most common case
            return top;
        } else {
            double alphaNormalized = alpha / 255.0;
            return (int) (top * alphaNormalized + bottom * (1.0 - alphaNormalized));
        }
    }

}
