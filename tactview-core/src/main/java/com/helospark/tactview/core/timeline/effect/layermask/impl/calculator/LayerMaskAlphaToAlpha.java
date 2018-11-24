package com.helospark.tactview.core.timeline.effect.layermask.impl.calculator;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskAlphaCalculator;

@Component
public class LayerMaskAlphaToAlpha implements LayerMaskAlphaCalculator {

    @Override
    public int calculateAlpha(ClipFrameResult maskToUse, int x, int y) {
        return maskToUse.getAlpha(x, y);
    }

    @Override
    public String getId() {
        return "alphaToAlpha";
    }

}
