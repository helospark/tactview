package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class LightenBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public LightenBlendModeStrategy() {
        super("lighten");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        return Math.max(topNormalized, bottomNormalized);
    }

}
