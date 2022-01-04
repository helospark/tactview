package com.helospark.tactview.core.timeline.blendmode;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

public abstract class AbstractHslBlendMode implements BlendModeStrategy {
    private String id;

    public AbstractHslBlendMode(String id) {
        this.id = id;
    }

    @Override
    public void computeColor(int[] topLayer, int[] bottomLayer, int[] resultOut) {
        Color topColor = new Color(topLayer[0] / 255.0, topLayer[1] / 255.0, topLayer[2] / 255.0);
        Color topHslColor = topColor.rgbToHsl();

        Color bottomColor = new Color(bottomLayer[0] / 255.0, bottomLayer[1] / 255.0, bottomLayer[2] / 255.0);
        Color bottomHslColor = bottomColor.rgbToHsl();

        double topAlpha = topLayer[3] / 255.0;
        double bottomAlpha = bottomLayer[3] / 255.0;

        Color modifiedHslColor = createMofidifedHslColor(topHslColor, bottomHslColor, topAlpha, bottomAlpha);

        Color rgbColor = modifiedHslColor.hslToRgbColor();
        resultOut[0] = (int) (rgbColor.red * 255.0);
        resultOut[1] = (int) (rgbColor.green * 255.0);
        resultOut[2] = (int) (rgbColor.blue * 255.0);
        resultOut[3] = topLayer[3];
    }

    protected abstract Color createMofidifedHslColor(Color hslColor, Color bottomHslColor, double topAlpha, double bottomAlpha);

    @Override
    public String getId() {
        return id;
    }
}
