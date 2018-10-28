package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;

public class IntegerProvider extends KeyframeableEffect {
    private Integer min = 0;
    private Integer max = Integer.MAX_VALUE;
    private MultiKeyframeBasedDoubleInterpolator interpolator;

    public IntegerProvider(Integer min, Integer max, MultiKeyframeBasedDoubleInterpolator interpolator) {
        this.min = min;
        this.max = max;
        this.interpolator = interpolator;
    }

    @Override
    public Integer getValueAt(TimelinePosition position) {
        Double value = interpolator.valueAt(position);
        int result = value.intValue();
        if (result < min) {
            return min;
        } else if (result > max) {
            return max;
        } else {
            return result; // todo: option for different interpolation
        }
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        interpolator.valueAdded(globalTimelinePosition, value);
    }

    @Override
    public void interpolatorChanged(EffectInterpolator newInterpolator) {
        this.interpolator = (MultiKeyframeBasedDoubleInterpolator) newInterpolator;
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        interpolator.valueRemoved(globalTimelinePosition);
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return interpolator.getValues();
    }

}
