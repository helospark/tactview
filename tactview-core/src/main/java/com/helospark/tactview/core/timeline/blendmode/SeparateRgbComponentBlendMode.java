package com.helospark.tactview.core.timeline.blendmode;

public abstract class SeparateRgbComponentBlendMode implements BlendModeStrategy {
    private String id;

    public SeparateRgbComponentBlendMode(String id) {
        this.id = id;
    }

    @Override
    public void computeColor(int[] topLayer, int[] bottomLayer, int[] resultOut) {
        for (int i = 0; i < 3; ++i) {
            resultOut[i] = computeSeparateComponent(topLayer[i], bottomLayer[i], topLayer[3]);
        }
        resultOut[3] = topLayer[3];
    }

    protected int computeSeparateComponent(int top, int bottom, int alpha) {
        double alphaNormalized = alpha / 255.0;
        double topNormalized = top / 255.0;
        double bottomNormalized = bottom / 255.0;

        return (int) (computeRgbComponent(topNormalized, bottomNormalized) * 255.0 * alphaNormalized + bottom * (1.0 - alphaNormalized));
    }

    public abstract double computeRgbComponent(double topLayer, double bottomLayer);

    @Override
    public String getId() {
        return id;
    }
}
