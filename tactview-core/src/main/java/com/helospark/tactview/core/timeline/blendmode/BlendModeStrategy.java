package com.helospark.tactview.core.timeline.blendmode;

public interface BlendModeStrategy {

    public void computeColor(int[] topLayer, int[] bottomLayer, int[] resultOut);

}
