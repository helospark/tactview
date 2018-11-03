package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class SubtractBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public SubtractBlendModeStrategy() {
        super("subtract");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        return topNormalized - bottomNormalized;
    }

}
