package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;

public class PointProviderFactory extends AbstractKeyframeableEffectDesSerFactory<PointProvider> {

    @Override
    public void addDataForDeserializeInternal(PointProvider instance, Map<String, Object> data) {
        data.put("xProvider", instance.xProvider);
        data.put("yProvider", instance.yProvider);
    }

    @Override
    public PointProvider deserializeInternal(JsonNode data, PointProvider currentFieldValue, LoadMetadata loadMetadata) {
        return new PointProvider(ReflectionUtil.deserialize(data.get("xProvider"), DoubleProvider.class, currentFieldValue.xProvider, loadMetadata),
                ReflectionUtil.deserialize(data.get("yProvider"), DoubleProvider.class, currentFieldValue.yProvider, loadMetadata));
    }

}
