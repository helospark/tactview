package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl;

import java.util.Collections;
import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class ConstantInterpolator extends KeyframeSupportingDoubleInterpolator {
    double value;

    public ConstantInterpolator(double defaultValue) {
        this.value = defaultValue;
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        return value;
    }

    @Override
    public void valueAddedInternal(TimelinePosition globalTimelinePosition, String value) {
        this.value = Double.valueOf(value);
    }

    @Override
    public void valueRemovedInternal(TimelinePosition globalTimelinePosition) {
        // not supported
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return Collections.emptyMap();
    }

    @Override
    public ConstantInterpolator deepClone() {
        return new ConstantInterpolator(value);
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return ConstantInterpolatorFactory.class;
    }

    @Override
    public void setDefaultValue(double defaultValue) {
        this.value = defaultValue;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        // Not supported
    }

    @Override
    public boolean isUsingKeyframes() {
        return false;
    }

    @Override
    public boolean supportsKeyframes() {
        return false; // this is most likely on the wrong interface
    }

}
