package com.helospark.tactview.core.timeline.valueprovidereffect.impl;

import java.util.List;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.core.timeline.valueprovidereffect.EffectValue;
import com.helospark.tactview.core.timeline.valueprovidereffect.StatelessValueProviderEffect;
import com.helospark.tactview.core.timeline.valueprovidereffect.StatelessValueProviderRequest;
import com.helospark.tactview.core.util.ReflectionUtil;

public class PivotPointValueProviderEffect extends StatelessValueProviderEffect {
    private PointProvider pointProvider;

    public PivotPointValueProviderEffect(TimelineInterval interval) {
        super(interval);
    }

    public PivotPointValueProviderEffect(PivotPointValueProviderEffect pivotPointValueProviderEffect) {
        super(pivotPointValueProviderEffect);
        ReflectionUtil.copyOrCloneFieldFromTo(pivotPointValueProviderEffect, this);
    }

    @Override
    public List<String> expectedValues() {
        return null;
    }

    @Override
    public List<EffectValue> getValuesAt(StatelessValueProviderRequest request) {
        Point point = pointProvider.getValueAt(request.getEffectPosition());

        return List.of(new EffectValue("pivot", point));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor pointDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(pointProvider)
                .withName("Pivot point")
                .build();

        return List.of(pointDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new PivotPointValueProviderEffect(this);
    }

    @Override
    public void initializeValueProvider() {
        pointProvider = new PointProvider(
                new DoubleProvider(SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(0.0)),
                new DoubleProvider(SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(0.0)));
    }

}
