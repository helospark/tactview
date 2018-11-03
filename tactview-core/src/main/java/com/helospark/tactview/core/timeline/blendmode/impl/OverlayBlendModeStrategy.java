package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class OverlayBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public OverlayBlendModeStrategy() {
        super("overlay");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        if (bottomNormalized < 0.5) {
            return (topNormalized * bottomNormalized * 2.0);
        } else {
            return ((1.0 - 2.0 * (1.0 - bottomNormalized) * (1.0 - topNormalized)));
        }
    }

}
