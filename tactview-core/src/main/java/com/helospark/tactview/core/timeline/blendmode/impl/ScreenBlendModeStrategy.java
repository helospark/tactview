package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class ScreenBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public ScreenBlendModeStrategy() {
        super("screen");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        return ((1.0 - (1.0 - topNormalized) * (1.0 - bottomNormalized)));
    }

}
