package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;

@Component
public class MultiKeyframeDoubleCurveEditor extends TypeSupportingPointBasedKeyframeDoubleCurveEditor<MultiKeyframeBasedDoubleInterpolator> {

    public MultiKeyframeDoubleCurveEditor() {
        super(MultiKeyframeBasedDoubleInterpolator.class);
    }

    @Override
    protected void valueModifiedAtInternal(MultiKeyframeBasedDoubleInterpolator currentKeyframeableEffect, TimelinePosition timelinePosition, TimelinePosition newTime, double newValue) {
        currentKeyframeableEffect.valueModifiedAt(timelinePosition, newTime, newValue);
    }

}
