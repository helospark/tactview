package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;

@Component
public class MultiKeyframeDoubleCurveEditor extends AbstractGeneralPointBasedCurveEditor {

    @Override
    public boolean supports(EffectInterpolator interpolator) {
        return interpolator instanceof MultiKeyframeBasedDoubleInterpolator;
    }

    @Override
    protected void valueModifiedAt(KeyframeSupportingDoubleInterpolator currentKeyframeableEffect, TimelinePosition timelinePosition, TimelinePosition newTime, double newValue) {
        ((MultiKeyframeBasedDoubleInterpolator) currentKeyframeableEffect).valueModifiedAt(timelinePosition, newTime, newValue);
    }

    @Override
    protected List<KeyframePoint> getKeyframePoints(KeyframeSupportingDoubleInterpolator effect) {
        Map<TimelinePosition, Object> values = ((MultiKeyframeBasedDoubleInterpolator) effect).getValues();
        return values.entrySet()
                .stream()
                .map(entry -> new KeyframePoint(entry.getKey(), (Double) entry.getValue()))
                .collect(Collectors.toList());
    }

}
