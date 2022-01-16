package com.helospark.tactview.core.timeline.blendmode.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.AbstractHslBlendMode;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

@Component
public class HslHueBlendMode extends AbstractHslBlendMode {

    public HslHueBlendMode() {
        super("HSL hue");
    }

    @Override
    protected Color createMofidifedHslColor(Color topHslColor, Color bottomHslColor, double topAlpha, double bottomAlpha) {
        double hue = topHslColor.getHue() * topAlpha + bottomHslColor.getHue() * (1.0 - topAlpha);
        double saturation = bottomHslColor.getSaturation();
        double lightness = bottomHslColor.getLightness();
        return new Color(hue, saturation, lightness);
    }

}
