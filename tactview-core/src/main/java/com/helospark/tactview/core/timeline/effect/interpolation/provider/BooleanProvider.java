package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;

public class BooleanProvider extends KeyframeableEffect {
    private DoubleInterpolator doubleInterpolator;

    public BooleanProvider(MultiKeyframeBasedDoubleInterpolator doubleInterpolator) {
        this.doubleInterpolator = doubleInterpolator;
    }

    @Override
    public Boolean getValueAt(TimelinePosition position) {
        Double value = doubleInterpolator.valueAt(position);
        return value > 0.5;
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        if (doubleInterpolator instanceof KeyframeSupportingDoubleInterpolator) {
            KeyframeSupportingDoubleInterpolator keyframeInterpolator = ((KeyframeSupportingDoubleInterpolator) doubleInterpolator);
            if (value.equalsIgnoreCase("true")) {
                keyframeInterpolator.valueAdded(globalTimelinePosition, "1.0");
            } else if (value.equalsIgnoreCase("false")) {
                keyframeInterpolator.valueAdded(globalTimelinePosition, "0.0");
            } else {
                keyframeInterpolator.valueAdded(globalTimelinePosition, value);
            }
        }
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

}
