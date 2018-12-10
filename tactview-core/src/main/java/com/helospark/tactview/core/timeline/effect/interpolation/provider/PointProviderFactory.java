package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SavedContentAddable;

public class PointProviderFactory implements DesSerFactory<PointProvider> {

    @Override
    public void addDataForDeserialize(PointProvider instance, Map<String, Object> data) {
        data.put("xProvider", instance.xProvider);
        data.put("yProvider", instance.yProvider);
    }

    @Override
    public PointProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        return new PointProvider(ReflectionUtil.deserialize(data.get("xProvider"), DoubleProvider.class),
                ReflectionUtil.deserialize(data.get("yProvider"), DoubleProvider.class));
    }

}
