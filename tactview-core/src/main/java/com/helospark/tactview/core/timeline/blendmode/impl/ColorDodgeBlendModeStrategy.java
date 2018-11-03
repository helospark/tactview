package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class ColorDodgeBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public ColorDodgeBlendModeStrategy() {
        super("color_dodge");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        if (topNormalized < 1.0) {
            return bottomNormalized / (1.0 - topNormalized);
        }
        return 1.0;
    }

}
