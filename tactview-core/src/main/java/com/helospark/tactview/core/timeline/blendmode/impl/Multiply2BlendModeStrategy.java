package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class Multiply2BlendModeStrategy extends SeparateRgbComponentBlendMode {

    public Multiply2BlendModeStrategy() {
        super("multiply2");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        return (bottomNormalized * topNormalized);
    }

}
