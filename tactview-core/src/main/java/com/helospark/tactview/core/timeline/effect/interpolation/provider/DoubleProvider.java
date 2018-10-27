package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

public class DoubleProvider extends KeyframeableEffect {
    private SizeFunction sizeFunction;
    private double min;
    private double max;
    private DoubleInterpolator interpolator;

    public DoubleProvider(double min, double max, DoubleInterpolator interpolator) {
        this.min = min;
        this.max = max;
        this.interpolator = interpolator;
        this.sizeFunction = SizeFunction.CLAMP_TO_MIN_MAX;
    }

    public DoubleProvider(SizeFunction sizeFunction, DoubleInterpolator interpolator) {
        this.sizeFunction = sizeFunction;
        this.interpolator = interpolator;
    }

    public DoubleProvider(DoubleInterpolator interpolator) {
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

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public SizeFunction getSizeFunction() {
        return sizeFunction;
    }

    @Override
    public boolean hasKeyframes() {
        return interpolator.hasKeyframes();
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return interpolator.getValues();
    }

}
