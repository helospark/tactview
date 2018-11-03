package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class ColorBurnBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public ColorBurnBlendModeStrategy() {
        super("color_burn");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        if (topNormalized > 0.0) {
            return 1.0 - ((1.0 - bottomNormalized) / topNormalized);
        }
        return 0.0;
    }

}
