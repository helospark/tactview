package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public interface PropertyValueSetterChainItem {

    public EffectLine create(ValueProviderDescriptor descriptor, KeyframeableEffect effect);

    public boolean doesSupport(KeyframeableEffect effect);
}
