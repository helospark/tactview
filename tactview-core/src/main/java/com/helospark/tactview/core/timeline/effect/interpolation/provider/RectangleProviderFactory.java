package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.helospark.tactview.core.util.DesSerFactory;

public class RectangleProviderFactory implements DesSerFactory<RectangleProvider> {

    @Override
    public void addDataForDeserialize(RectangleProvider instance, Map<String, Object> data) {
        data.put("pointProviders", data);
    }

    @Override
    public RectangleProvider deserialize(Map<String, Object> data) {
        List<Map<String, Object>> deserializedData = new ArrayList<>();
        List<PointProvider> points = new ArrayList<>();

        for (var map : deserializedData) {
            // TODO:
        }

        return new RectangleProvider(points);
    }

}
