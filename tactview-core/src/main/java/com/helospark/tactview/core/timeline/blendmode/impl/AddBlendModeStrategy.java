package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class AddBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public AddBlendModeStrategy() {
        super("add");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        return topNormalized + bottomNormalized;
    }

}
