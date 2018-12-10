package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SavedContentAddable;

public class IntegerProviderFactory implements DesSerFactory<IntegerProvider> {

    @Override
    public void addDataForDeserialize(IntegerProvider instance, Map<String, Object> data) {
        data.put("min", instance.min);
        data.put("max", instance.max);
        data.put("interpolator", instance.interpolator);
    }

    @Override
    public IntegerProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        IntegerProvider result = new IntegerProvider(data.get("min").asInt(), data.get("max").asInt(),
                ReflectionUtil.deserialize(data.get("interpolator"), DoubleInterpolator.class));
        return result;
    }

}
