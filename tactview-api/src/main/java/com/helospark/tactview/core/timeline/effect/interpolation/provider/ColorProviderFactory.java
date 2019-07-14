package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;

public class ColorProviderFactory extends AbstractKeyframeableEffectDesSerFactory<ColorProvider> {

    @Override
    public void addDataForDeserializeInternal(ColorProvider instance, Map<String, Object> data) {
        data.put("redProvider", instance.redProvider);
        data.put("greenProvider", instance.greenProvider);
        data.put("blueProvider", instance.blueProvider);
    }

    @Override
    public ColorProvider deserializeInternal(JsonNode data, ColorProvider currentFieldValue, LoadMetadata loadMetadata) {
        return new ColorProvider(ReflectionUtil.deserialize(data.get("redProvider"), DoubleProvider.class, currentFieldValue.redProvider, loadMetadata),
                ReflectionUtil.deserialize(data.get("greenProvider"), DoubleProvider.class, currentFieldValue.greenProvider, loadMetadata),
                ReflectionUtil.deserialize(data.get("blueProvider"), DoubleProvider.class, currentFieldValue.blueProvider, loadMetadata));
    }

}
