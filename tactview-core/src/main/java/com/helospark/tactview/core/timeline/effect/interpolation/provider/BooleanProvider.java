package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;

public class BooleanProvider extends KeyframeableEffect {
    private DoubleInterpolator doubleInterpolator;

    public BooleanProvider(DoubleInterpolator doubleInterpolator) {
        this.doubleInterpolator = doubleInterpolator;
    }

    @Override
    public Boolean getValueAt(TimelinePosition position) {
        Double value = doubleInterpolator.valueAt(position);
        return value > 0.5;
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        if (value.equalsIgnoreCase("true")) {
            doubleInterpolator.valueAdded(globalTimelinePosition, "1.0");
        } else if (value.equalsIgnoreCase("false")) {
            doubleInterpolator.valueAdded(globalTimelinePosition, "0.0");
        } else {
            doubleInterpolator.valueAdded(globalTimelinePosition, value);
        }
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean hasKeyframes() {
        return doubleInterpolator.hasKeyframes();
    }
}
