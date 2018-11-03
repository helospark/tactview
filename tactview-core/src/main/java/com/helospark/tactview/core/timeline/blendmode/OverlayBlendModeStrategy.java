package com.helospark.tactview.core.timeline.blendmode;

public class OverlayBlendModeStrategy implements BlendModeStrategy {

    @Override
    public void computeColor(int[] topLayer, int[] bottomLayer, int[] resultOut) {
        for (int i = 0; i < 3; ++i) {
            resultOut[i] = calculateOverlay(bottomLayer[i], topLayer[i], topLayer[3]);
        }
        resultOut[3] = topLayer[3];
    }

    private int calculateOverlay(int bottom, int top, int alpha) {
        double alphaNormalized = alpha / 255.0;
        return (int) (overlay(bottom, top) * alphaNormalized + top * (1.0 - alphaNormalized));
    }

    private int overlay(int bottom, int top) {
        double topNormalized = top / 255.0;
        double bottomNormalized = bottom / 255.0;
        if (bottomNormalized < 0.5) {
            return (int) (topNormalized * bottomNormalized * 255.0 * 2.0);
        } else {
            return (int) ((1.0 - 2.0 * (1.0 - bottomNormalized) * (1.0 - topNormalized)) * 255.0);
        }
    }

}
