package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Collections;
import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;

public class DoubleProvider extends KeyframeableEffect {
    private SizeFunction sizeFunction;
    private double min;
    private double max;
    private DoubleInterpolator interpolator;

    public DoubleProvider(double min, double max, MultiKeyframeBasedDoubleInterpolator interpolator) {
        this.min = min;
        this.max = max;
        this.interpolator = interpolator;
        this.sizeFunction = SizeFunction.CLAMP_TO_MIN_MAX;
    }

    public DoubleProvider(SizeFunction sizeFunction, MultiKeyframeBasedDoubleInterpolator interpolator) {
        this.sizeFunction = sizeFunction;
        this.interpolator = interpolator;
    }

    public DoubleProvider(MultiKeyframeBasedDoubleInterpolator interpolator) {
        this.sizeFunction = SizeFunction.NO_TRANSFORMATION;
        this.interpolator = interpolator;
    }

    @Override
    public Double getValueAt(TimelinePosition position) {
        Double value = interpolator.valueAt(position);
        if (sizeFunction.equals(SizeFunction.CLAMP_TO_MIN_MAX)) {
            if (value < min) {
                return min;
            } else if (value > max) {
                return max;
            } else {
                return value;
            }
        } else {
            return value;
        }
    }

    public void setInterpolator(MultiKeyframeBasedDoubleInterpolator interpolator) {
        this.interpolator = interpolator;
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        if (interpolator instanceof KeyframeSupportingDoubleInterpolator) {
            ((KeyframeSupportingDoubleInterpolator) interpolator).valueAdded(globalTimelinePosition, value);
        }
    }

    @Override
    public void interpolatorChanged(EffectInterpolator newInterpolator) {
        this.interpolator = (MultiKeyframeBasedDoubleInterpolator) newInterpolator;
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        if (interpolator instanceof KeyframeSupportingDoubleInterpolator) {
            ((KeyframeSupportingDoubleInterpolator) interpolator).valueRemoved(globalTimelinePosition);
        }
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public SizeFunction getSizeFunction() {
        return sizeFunction;
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        if (interpolator instanceof KeyframeSupportingDoubleInterpolator) {
            return ((KeyframeSupportingDoubleInterpolator) interpolator).getValues();
        } else {
            return Collections.emptyMap();
        }
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public EffectInterpolator getInterpolator() {
        return interpolator.cloneInterpolator();
    }

    @Override
    public void setInterpolator(Object previousInterpolator) {
        this.interpolator = (DoubleInterpolator) previousInterpolator;
    }

}
