package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public abstract class TypeBasedPropertyValueSetterChainItem<T extends KeyframeableEffect> implements PropertyValueSetterChainItem {
    private Class<T> supportedType;

    public TypeBasedPropertyValueSetterChainItem(Class<T> supportedType) {
        this.supportedType = supportedType;
    }

    @Override
    public EffectLine create(ValueProviderDescriptor descriptor, KeyframeableEffect effect) {
        return handle(supportedType.cast(effect), descriptor);
    }

    protected abstract EffectLine handle(T effect, ValueProviderDescriptor descriptor);

    @Override
    public boolean doesSupport(KeyframeableEffect effect) {
        return supportedType.isAssignableFrom(effect.getClass());
    }

}
