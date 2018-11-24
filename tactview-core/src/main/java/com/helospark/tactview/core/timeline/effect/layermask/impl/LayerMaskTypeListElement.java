package com.helospark.tactview.core.timeline.effect.layermask.impl;

import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;

public class LayerMaskTypeListElement extends ValueListElement {

    private LayerMaskAlphaCalculator layerMaskAlphaCalculator;

    public LayerMaskTypeListElement(LayerMaskAlphaCalculator layerMaskAlphaCalculator, String id, String text) {
        super(id, text);
        this.layerMaskAlphaCalculator = layerMaskAlphaCalculator;
    }

    public LayerMaskAlphaCalculator getLayerMaskAlphaCalculator() {
        return layerMaskAlphaCalculator;
    }

}
