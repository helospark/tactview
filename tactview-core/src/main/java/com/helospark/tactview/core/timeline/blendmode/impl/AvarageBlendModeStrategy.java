package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class AvarageBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public AvarageBlendModeStrategy() {
        super("avarage");
    }

    @Override
    public double computeRgbComponent(double topNormalized, double bottomNormalized) {
        return (topNormalized + bottomNormalized) / 2.0;
    }

}
