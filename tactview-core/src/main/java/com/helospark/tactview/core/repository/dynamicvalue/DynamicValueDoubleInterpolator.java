package com.helospark.tactview.core.repository.dynamicvalue;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.valueprovidereffect.StatelessValueProviderEffect;
import com.helospark.tactview.core.timeline.valueprovidereffect.StatelessValueProviderRequest;
import com.helospark.tactview.core.util.DesSerFactory;

public class DynamicValueDoubleInterpolator implements DoubleInterpolator {
    public StatelessValueProviderEffect effect;

    public DynamicValueDoubleInterpolator(StatelessValueProviderEffect effect) {
        this.effect = effect;
    }

    @Override
    public DynamicValueDoubleInterpolator deepClone() {
        return new DynamicValueDoubleInterpolator(effect);
    }

    @Override
    public Double valueAt(TimelinePosition position) {
        // TODO: this needs finishing
        StatelessValueProviderRequest provider = StatelessValueProviderRequest.builder()
                .withEffectPosition(position)
                .build();
        Double result = (Double) effect.getValuesAt(provider).get(0).value;
        return 0.0;
    }

    @Override
    public Class<? extends DesSerFactory<? extends EffectInterpolator>> generateSerializableContent() {
        return null; // TODO:
    }

}
