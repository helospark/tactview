package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class MultiplyBlendModeStrategy extends SeparateRgbComponentBlendMode {
    public static final String ID = "multiply";

    public MultiplyBlendModeStrategy() {
        super(ID);
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        return 1.0 - Math.abs(1.0 - bottomNormalized - topNormalized);
    }

}
