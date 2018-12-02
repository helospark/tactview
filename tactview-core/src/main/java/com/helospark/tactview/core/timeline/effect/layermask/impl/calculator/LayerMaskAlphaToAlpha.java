package com.helospark.tactview.core.timeline.effect.layermask.impl.calculator;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.layermask.impl.LayerMaskAlphaCalculator;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

@Component
public class LayerMaskAlphaToAlpha implements LayerMaskAlphaCalculator {

    @Override
    public int calculateAlpha(ReadOnlyClipImage maskToUse, int x, int y) {
        return maskToUse.getAlpha(x, y);
    }

    @Override
    public String getId() {
        return "alphaToAlpha";
    }

}
