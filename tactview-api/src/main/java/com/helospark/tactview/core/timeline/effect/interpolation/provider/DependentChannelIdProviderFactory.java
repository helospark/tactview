package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;

public class DependentChannelIdProviderFactory extends AbstractKeyframeableEffectDesSerFactory<DependentChannelIdProvider> {

    @Override
    public void addDataForDeserializeInternal(DependentChannelIdProvider instance, Map<String, Object> data) {
        data.put("stringInterpolator", instance.stringInterpolator);
    }

    @Override
    public DependentChannelIdProvider deserializeInternal(JsonNode data, DependentChannelIdProvider currentFieldValue, LoadMetadata loadMetadata) {
        return new DependentChannelIdProvider(ReflectionUtil.deserialize(data.get("stringInterpolator"), StepStringInterpolator.class, currentFieldValue.stringInterpolator, loadMetadata));
    }

}
