package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;

public interface PropertyValueSetterChainItem {

    public EffectLine create(KeyframeableEffect effect);

    public boolean doesSupport(KeyframeableEffect effect);
}
