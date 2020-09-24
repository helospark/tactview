package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;

public class ConstantInterpolatorFactory implements DesSerFactory<ConstantInterpolator> {

    @Override
    public void addDataForDeserialize(ConstantInterpolator instance, Map<String, Object> data) {
        data.put("value", instance.value);
        data.put("initialValue", instance.initialValue);
    }

    @Override
    public ConstantInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            double value = data.get("value").asDouble();
            double initialValue = data.get("initialValue").asDouble();
            ConstantInterpolator result = new ConstantInterpolator(value);
            result.initialValue = initialValue;
            return result;
        } catch (Exception e) {
            if (currentFieldValue instanceof ConstantInterpolator) {
                return (ConstantInterpolator) currentFieldValue;
            } else {
                throw new RuntimeException("Unable to load field");
            }
        }
    }
}
