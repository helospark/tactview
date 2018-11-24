package com.helospark.tactview.core.timeline.effect.layermask.impl;

import com.helospark.tactview.core.timeline.ClipFrameResult;

public interface LayerMaskAlphaCalculator {

    public int calculateAlpha(ClipFrameResult maskToUse, int x, int y);

    public String getId();
}
