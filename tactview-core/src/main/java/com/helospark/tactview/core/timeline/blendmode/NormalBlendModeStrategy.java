package com.helospark.tactview.core.timeline.blendmode;

public class NormalBlendModeStrategy implements BlendModeStrategy {

    @Override
    public void computeColor(int[] topLayer, int[] bottomLayer, int[] resultOut) {
        for (int i = 0; i < 4; ++i) {
            resultOut[i] = topLayer[i];
        }
    }

}
