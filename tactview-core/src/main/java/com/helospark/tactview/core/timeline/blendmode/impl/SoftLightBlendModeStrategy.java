package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.SeparateRgbComponentBlendMode;

@Component
public class SoftLightBlendModeStrategy extends SeparateRgbComponentBlendMode {

    public SoftLightBlendModeStrategy() {
        super("soft_light");
    }

    @Override
    public double computeRgbComponent(double blend, double base) {
        return (blend < 0.5) ? (2.0 * base * blend + base * base * (1.0 - 2.0 * blend)) : (Math.sqrt(base) * (2.0 * blend - 1.0) + 2.0 * base * (1.0 - blend));
    }

}
