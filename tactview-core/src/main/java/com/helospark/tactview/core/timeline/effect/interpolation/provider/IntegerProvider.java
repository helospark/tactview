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

public class IntegerProvider extends KeyframeableEffect {
    Integer min = 0;
    Integer max = Integer.MAX_VALUE;
    DoubleInterpolator interpolator;

    public IntegerProvider(Integer min, Integer max, DoubleInterpolator interpolator) {
        this.min = min;
        this.max = max;
        this.interpolator = interpolator;
    }

    public IntegerProvider(MultiKeyframeBasedDoubleInterpolator interpolator) {
        this.min = Integer.MIN_VALUE;
        this.max = Integer.MAX_VALUE;
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
        if (interpolator instanceof KeyframeSupportingDoubleInterpolator) {
            ((KeyframeSupportingDoubleInterpolator) interpolator).valueAddedInternal(globalTimelinePosition, value);
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
        if (interpolator instanceof KeyframeSupportingDoubleInterpolator) {
            return ((KeyframeSupportingDoubleInterpolator) interpolator).getValues();
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public IntegerProvider deepClone() {
        IntegerProvider result = new IntegerProvider(min, max, interpolator.deepClone());
        return result;
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect>> generateSerializableContent() {
        return IntegerProviderFactory.class;
    }

    @Override
    public boolean supportsKeyframes() {
        return interpolator instanceof KeyframeSupportingInterpolator;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        ((KeyframeSupportingInterpolator) interpolator).setUseKeyframes(useKeyframes);
    }

    @Override
    public boolean keyframesEnabled() {
        return ((KeyframeSupportingInterpolator) interpolator).isUsingKeyframes();
    }

    @Override
    public EffectInterpolator getInterpolator() {
        return interpolator;
    }
}
