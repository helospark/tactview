package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.AbstractHslBlendMode;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

@Component
public class HslLightnessBlendMode extends AbstractHslBlendMode {

    public HslLightnessBlendMode() {
        super("HSL lightness");
    }

    @Override
    protected Color createMofidifedHslColor(Color topHslColor, Color bottomHslColor, double topAlpha, double bottomAlpha) {
        double hue = bottomHslColor.getHue();
        double saturation = bottomHslColor.getSaturation();
        double lightness = topHslColor.getLightness() * topAlpha + bottomHslColor.getLightness() * (1.0 - topAlpha);
        return new Color(hue, saturation, lightness);
    }

}
