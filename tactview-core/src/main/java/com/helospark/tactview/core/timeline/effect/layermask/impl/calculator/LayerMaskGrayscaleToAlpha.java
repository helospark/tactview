package com.helospark.tactview.core.timeline.effect.layermask.impl.calculator;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskAlphaCalculator;

@Component
public class LayerMaskGrayscaleToAlpha implements LayerMaskAlphaCalculator {

    @Override
    public int calculateAlpha(ClipFrameResult maskToUse, int x, int y) {
        return (maskToUse.getRed(x, y) + maskToUse.getGreen(x, y) + maskToUse.getBlue(x, y)) / 3;
    }

    @Override
    public String getId() {
        return "grayscaleToAlpha";
    }

}
