package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.EffectLine;

public class PropertyValueContextMenuRequest {
    public KeyframeableEffect valueProvider;
    public ValueProviderDescriptor containerDescriptor;
    public EffectLine effectLine;
    public TimelinePosition timelinePosition;

    public PropertyValueContextMenuRequest(KeyframeableEffect doubleProvider, ValueProviderDescriptor descriptor, EffectLine effectLine, TimelinePosition timelinePosition) {
        this.valueProvider = doubleProvider;
        this.containerDescriptor = descriptor;
        this.effectLine = effectLine;
        this.timelinePosition = timelinePosition;
    }

}
