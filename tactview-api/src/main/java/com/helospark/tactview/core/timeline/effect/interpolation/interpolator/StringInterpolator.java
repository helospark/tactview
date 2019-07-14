package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;

public interface StringInterpolator extends EffectInterpolator, KeyframeSupportingInterpolator {

    @Override
    String valueAt(TimelinePosition position);

    void valueAdded(TimelinePosition globalTimelinePosition, String value);

    void removeKeyframeAt(TimelinePosition globalTimelinePosition);

    boolean hasKeyframes();

    Map<TimelinePosition, Object> getValues();

    boolean useKeyframes();

    String getDefaultValue();

    @Override
    StringInterpolator deepClone();

}