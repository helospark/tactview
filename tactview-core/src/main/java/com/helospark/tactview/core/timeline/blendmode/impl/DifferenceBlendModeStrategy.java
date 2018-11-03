package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class DifferenceBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public DifferenceBlendModeStrategy() {
        super("difference");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        return Math.abs(bottomNormalized - topNormalized);
    }

}
