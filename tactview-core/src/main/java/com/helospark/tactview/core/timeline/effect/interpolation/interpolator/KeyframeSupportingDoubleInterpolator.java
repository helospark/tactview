package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;

public interface KeyframeSupportingDoubleInterpolator extends DoubleInterpolator {

    public void valueAdded(TimelinePosition globalTimelinePosition, String value);

    public void valueRemoved(TimelinePosition globalTimelinePosition);

    public Map<TimelinePosition, Object> getValues();

    public void setDefaultValue(double defaultValue);

}
