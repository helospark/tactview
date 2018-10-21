package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;

public abstract class TypeBasedPropertyValueSetterChainItem<T extends KeyframeableEffect> implements PropertyValueSetterChainItem {
    private Class<T> supportedType;

    public TypeBasedPropertyValueSetterChainItem(Class<T> supportedType) {
        this.supportedType = supportedType;
    }

    @Override
    public EffectLine create(KeyframeableEffect effect) {
        return handle(supportedType.cast(effect));
    }

    protected abstract EffectLine handle(T effect);

    @Override
    public boolean doesSupport(KeyframeableEffect effect) {
        return supportedType.isAssignableFrom(effect.getClass());
    }

}
