package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SavedContentAddable;

public class LineProviderFactory implements DesSerFactory<LineProvider> {

    @Override
    public void addDataForDeserialize(LineProvider instance, Map<String, Object> data) {
        data.put("startPointProvider", instance.startPointProvider);
        data.put("endPointProvider", instance.endPointProvider);
    }

    @Override
    public LineProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        return new LineProvider(ReflectionUtil.deserialize(data.get("startPointProvider"), PointProvider.class),
                ReflectionUtil.deserialize(data.get("endPointProvider"), PointProvider.class));
    }

}
