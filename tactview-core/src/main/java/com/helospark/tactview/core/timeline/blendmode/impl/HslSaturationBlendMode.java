package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.AbstractHslBlendMode;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

@Component
public class HslSaturationBlendMode extends AbstractHslBlendMode {

    public HslSaturationBlendMode() {
        super("HSL saturation");
    }

    @Override
    protected Color createMofidifedHslColor(Color topHslColor, Color bottomHslColor, double topAlpha, double bottomAlpha) {
        double hue = bottomHslColor.getHue();
        double saturation = topHslColor.getSaturation() * topAlpha + bottomHslColor.getSaturation() * (1.0 - topAlpha);
        double lightness = bottomHslColor.getLightness();
        return new Color(hue, saturation, lightness);
    }

}
