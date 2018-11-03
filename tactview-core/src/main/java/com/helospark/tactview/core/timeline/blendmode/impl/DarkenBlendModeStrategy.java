package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class DarkenBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public DarkenBlendModeStrategy() {
        super("darken");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        return Math.min(topNormalized, bottomNormalized);
    }

}
