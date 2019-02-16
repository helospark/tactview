package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

@Component
public class PropertyValueSetterChain {
    private List<PropertyValueSetterChainItem> items;

    public PropertyValueSetterChain(List<PropertyValueSetterChainItem> items) {
        this.items = items;
    }

    public EffectLine create(ValueProviderDescriptor descriptor) {
        EffectLine result = createEffectLine(descriptor);

        return result;
    }

    private EffectLine createEffectLine(ValueProviderDescriptor descriptor) {
        return items.stream()
                .filter(e -> e.doesSupport(descriptor.getKeyframeableEffect()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No chain item for " + descriptor))
                .create(descriptor, descriptor.getKeyframeableEffect());
    }

}
