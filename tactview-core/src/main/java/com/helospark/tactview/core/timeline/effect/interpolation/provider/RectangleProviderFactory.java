package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.AbstractKeyframeableEffectDesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;

public class RectangleProviderFactory extends AbstractKeyframeableEffectDesSerFactory<RectangleProvider> {

    @Override
    public void addDataForDeserializeInternal(RectangleProvider instance, Map<String, Object> data) {
        data.put("pointProviders", instance.pointProviders);
    }

    @Override
    public RectangleProvider deserializeInternal(JsonNode data, RectangleProvider currentFieldValue, LoadMetadata loadMetadata) {
        List<PointProvider> points = new ArrayList<>();

        for (int i = 0; i < 6; ++i) {
            PointProvider point = ReflectionUtil.deserialize(data.get(i), PointProvider.class, currentFieldValue.pointProviders.get(i), loadMetadata);
            points.add(point);
        }

        return new RectangleProvider(points);
    }

}
