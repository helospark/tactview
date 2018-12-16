package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Collections;
import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class DoubleProvider extends KeyframeableEffect {
    SizeFunction sizeFunction;
    double min;
    double max;
    DoubleInterpolator interpolator;

    public DoubleProvider(double min, double max, DoubleInterpolator doubleInterpolator) {
        this.min = min;
        this.max = max;
        this.interpolator = doubleInterpolator;
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
        return interpolator.deepClone();
    }

    @Override
    public void setInterpolator(Object previousInterpolator) {
        this.interpolator = (DoubleInterpolator) previousInterpolator;
    }

    @Override
    public DoubleProvider deepClone() {
        DoubleProvider result = new DoubleProvider(min, max, interpolator.deepClone());
        result.sizeFunction = sizeFunction;
        return result;
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect>> generateSerializableContent() {
        return DoubleProviderFactory.class;
    }

    @Override
    public boolean supportsKeyframes() {
        return interpolator instanceof KeyframeSupportingInterpolator;
    }

    @Override
    public boolean keyframesEnabled() {
        return ((KeyframeSupportingInterpolator) interpolator).isUsingKeyframes();
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        ((KeyframeSupportingInterpolator) interpolator).setUseKeyframes(useKeyframes);
    }

    public void setDefaultValue(double defaultValue) {
        ((KeyframeSupportingDoubleInterpolator) interpolator).setDefaultValue(defaultValue);
    }

}
