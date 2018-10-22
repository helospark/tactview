package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

public class BooleanProvider extends KeyframeableEffect {
    private DoubleProvider doubleProvider;

    @Override
    public Object getValueAt(TimelinePosition position) {
        Double value = doubleProvider.getValueAt(position);
        return value > 0.5;
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        if (value.equalsIgnoreCase("true")) {
            doubleProvider.keyframeAdded(globalTimelinePosition, "1.0");
        } else if (value.equalsIgnoreCase("false")) {
            doubleProvider.keyframeAdded(globalTimelinePosition, "0.0");
        } else {
            System.out.println("Unexpeced value " + value);
        }
    }

    @Override
    public void interpolatorChanged(EffectInterpolator newInterpolator) {
        this.doubleProvider = (DoubleProvider) newInterpolator;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }
}
