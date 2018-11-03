package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class ExclusionBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public ExclusionBlendModeStrategy() {
        super("exclusion");
    }

    @Override
    public double computeRgbComponent(double topLayer, double bottomLayer) {
        return bottomLayer + topLayer - 2.0 * bottomLayer * topLayer;
    }

}
