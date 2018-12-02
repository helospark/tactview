package com.helospark.tactview.core.timeline.effect.layermask.impl;

import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public interface LayerMaskAlphaCalculator {

    public int calculateAlpha(ReadOnlyClipImage maskToUse, int x, int y);

    public String getId();
}
