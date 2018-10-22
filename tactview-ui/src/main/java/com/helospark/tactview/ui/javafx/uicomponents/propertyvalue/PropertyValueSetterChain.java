package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;

@Component
public class PropertyValueSetterChain {
    private List<PropertyValueSetterChainItem> items;

    public PropertyValueSetterChain(List<PropertyValueSetterChainItem> items) {
        this.items = items;
    }

    public EffectLine create(KeyframeableEffect effect) {
        return items.stream()
                .filter(e -> e.doesSupport(effect))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No chain item for " + effect))
                .create(effect);
    }
}
