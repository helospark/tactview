package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;

public class DoubleRangeProviderFactory extends AbstractKeyframeableEffectDesSerFactory<DoubleRangeProvider> {

    @Override
    protected void addDataForDeserializeInternal(DoubleRangeProvider instance, Map<String, Object> data) {
        data.put("lowEnd", instance.lowEndProvider);
        data.put("highEnd", instance.highEndProvider);
    }

    @Override
    protected DoubleRangeProvider deserializeInternal(JsonNode data, DoubleRangeProvider currentFieldValue, LoadMetadata loadMetadata) {
        return new DoubleRangeProvider(ReflectionUtil.deserialize(data.get("lowEnd"), DoubleProvider.class, currentFieldValue.lowEndProvider, loadMetadata),
                ReflectionUtil.deserialize(data.get("highEnd"), DoubleProvider.class, currentFieldValue.highEndProvider, loadMetadata));
    }

}
