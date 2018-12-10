package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.SavedContentAddable;

public class DoubleProviderFactory implements DesSerFactory<DoubleProvider> {

    @Override
    public void addDataForDeserialize(DoubleProvider instance, Map<String, Object> data) {
        data.put("sizeFunction", instance.sizeFunction);
        data.put("min", instance.min);
        data.put("max", instance.max);
        data.put("interpolator", instance.interpolator);
    }

    @Override
    public DoubleProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        DoubleProvider result = new DoubleProvider(data.get("min").asDouble(), data.get("max").asDouble(),
                ReflectionUtil.deserialize(data.get("interpolator"), DoubleInterpolator.class));
        result.sizeFunction = SizeFunction.valueOf(data.get("sizeFunction").asText());
        return result;
    }

}
