package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SavedContentAddable;

public class ColorProviderFactory implements DesSerFactory<ColorProvider> {

    @Override
    public void addDataForDeserialize(ColorProvider instance, Map<String, Object> data) {
        data.put("redProvider", instance.redProvider);
        data.put("greenProvider", instance.greenProvider);
        data.put("blueProvider", instance.blueProvider);
    }

    @Override
    public ColorProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        return new ColorProvider(ReflectionUtil.deserialize(data.get("redProvider"), DoubleProvider.class),
                ReflectionUtil.deserialize(data.get("greenProvider"), DoubleProvider.class),
                ReflectionUtil.deserialize(data.get("blueProvider"), DoubleProvider.class));
    }

}
