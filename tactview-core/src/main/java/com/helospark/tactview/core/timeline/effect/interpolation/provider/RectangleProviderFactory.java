package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SavedContentAddable;

public class RectangleProviderFactory implements DesSerFactory<RectangleProvider> {

    @Override
    public void addDataForDeserialize(RectangleProvider instance, Map<String, Object> data) {
        data.put("pointProviders", data);
    }

    @Override
    public RectangleProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        List<PointProvider> points = new ArrayList<>();

        for (int i = 0; i < 6; ++i) {
            PointProvider point = ReflectionUtil.deserialize(data.get(i), PointProvider.class);
            points.add(point);
        }

        return new RectangleProvider(points);
    }

}
