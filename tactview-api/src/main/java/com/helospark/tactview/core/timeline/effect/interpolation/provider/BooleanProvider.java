package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;

public class BooleanProvider extends KeyframeableEffect {
    DoubleInterpolator doubleInterpolator;

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
        if (doubleInterpolator instanceof KeyframeSupportingDoubleInterpolator) {
            KeyframeSupportingDoubleInterpolator keyframeInterpolator = ((KeyframeSupportingDoubleInterpolator) doubleInterpolator);
            if (value.equalsIgnoreCase("true")) {
                keyframeInterpolator.valueAddedInternal(globalTimelinePosition, "1.0");
            } else if (value.equalsIgnoreCase("false")) {
                keyframeInterpolator.valueAddedInternal(globalTimelinePosition, "0.0");
            } else {
                keyframeInterpolator.valueAddedInternal(globalTimelinePosition, value);
            }
        }
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public BooleanProvider deepClone() {
        return new BooleanProvider(doubleInterpolator.deepClone());
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect>> generateSerializableContent() {
        return BooleanProviderFactory.class;
    }

    @Override
    public boolean supportsKeyframes() {
        return (doubleInterpolator instanceof KeyframeSupportingInterpolator && ((KeyframeSupportingDoubleInterpolator) doubleInterpolator).supportsKeyframes());
    }

    @Override
    public boolean keyframesEnabled() {
        return ((KeyframeSupportingInterpolator) doubleInterpolator).isUsingKeyframes();
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        ((KeyframeSupportingInterpolator) doubleInterpolator).setUseKeyframes(useKeyframes);
    }

    @Override
    public void setInterpolator(Object interpolator) {
        this.doubleInterpolator = (DoubleInterpolator) interpolator;
    }

    @Override
    public EffectInterpolator getInterpolator() {
        return doubleInterpolator;
    }
}
