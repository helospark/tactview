package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

public class DoubleProvider extends KeyframeableEffect {
    private double min = -Double.MAX_VALUE;
    private double max = Double.MAX_VALUE;
    private DoubleInterpolator interpolator;

    public DoubleProvider(DoubleInterpolator interpolator) {
        this.interpolator = interpolator;
    }

    @Override
    public Double getValueAt(TimelinePosition position) {
        Double value = interpolator.valueAt(position);
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    public void setInterpolator(DoubleInterpolator interpolator) {
        this.interpolator = interpolator;
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        interpolator.valueAdded(globalTimelinePosition, value);
    }

    @Override
    public void interpolatorChanged(EffectInterpolator newInterpolator) {
        this.interpolator = (DoubleInterpolator) newInterpolator;
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        interpolator.valueRemoved(globalTimelinePosition);
    }

}
