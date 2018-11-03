package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class ReflectBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public ReflectBlendModeStrategy() {
        super("reflect");
    }

    @Override
    public double computeRgbComponent(double topLayer, double bottomLayer) {
        return (topLayer == 1.0) ? topLayer : Math.min(bottomLayer * bottomLayer / (1.0 - topLayer), 1.0);
    }

}
