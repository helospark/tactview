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

}
