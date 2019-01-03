package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;

public class ConstantInterpolatorFactory implements DesSerFactory<ConstantInterpolator> {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void addDataForDeserialize(ConstantInterpolator instance, Map<String, Object> data) {
        try {
            data.put("instance", objectMapper.writeValueAsString(instance));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConstantInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            return objectMapper.readValue(data.get("instance").textValue(), ConstantInterpolator.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
