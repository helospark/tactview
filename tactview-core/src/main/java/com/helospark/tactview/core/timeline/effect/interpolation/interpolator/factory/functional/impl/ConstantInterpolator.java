package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.impl;

import java.util.Collections;
import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;

public class ConstantInterpolator implements KeyframeSupportingDoubleInterpolator {
    private double value;

    public ConstantInterpolator(double defaultValue) {
        this.value = defaultValue;
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        return value;
    }

    @Override
    public void valueAdded(TimelinePosition globalTimelinePosition, String value) {
        this.value = Double.valueOf(value);
    }

    @Override
    public void valueRemoved(TimelinePosition globalTimelinePosition) {
        // not supported
    }

    @Override
    public boolean hasKeyframes() {
        return false;
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return Collections.emptyMap();
    }

    @Override
    public EffectInterpolator cloneInterpolator() {
        return new ConstantInterpolator(value);
    }
}
