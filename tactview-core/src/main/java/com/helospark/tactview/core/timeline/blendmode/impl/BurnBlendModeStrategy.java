package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class BurnBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public BurnBlendModeStrategy() {
        super("burn_blend");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        return topNormalized + bottomNormalized - 1.0;
    }

}
