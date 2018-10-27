package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;

public class StringProvider extends KeyframeableEffect {
    private StringInterpolator stringInterpolator;

    public StringProvider(StringInterpolator stringInterpolator) {
        this.stringInterpolator = stringInterpolator;
    }

    @Override
    public String getValueAt(TimelinePosition position) {
        return stringInterpolator.valueAt(position);
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        stringInterpolator.valueAdded(globalTimelinePosition, value);
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        stringInterpolator.removeKeyframeAt(globalTimelinePosition);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean hasKeyframes() {
        return stringInterpolator.hasKeyframes();
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return stringInterpolator.getValues();
    }

}
