package com.helospark.tactview.core.timeline.blendmode;

public class ScreenBlendModeStrategy implements BlendModeStrategy {

    @Override
    public void computeColor(int[] topLayer, int[] bottomLayer, int[] resultOut) {
        for (int i = 0; i < 3; ++i) {
            resultOut[i] = computeScreenAlpha(topLayer[i], bottomLayer[i], topLayer[3]);
        }
        resultOut[3] = topLayer[3];
    }

    public int computeScreenAlpha(int top, int bottom, int alpha) {
        double alphaNormalized = alpha / 255.0;
        double topNormalized = top / 255.0;
        double bottomNormalized = bottom / 255.0;

        return (int) (computeScreen(topNormalized, bottomNormalized) * alphaNormalized + bottom * (1.0 - alphaNormalized));
    }

    public int computeScreen(double topNormalized, double bottomNormalized) {
        return (int) ((1.0 - (1.0 - topNormalized) * (1.0 - bottomNormalized)) * 255.0);
    }

}
