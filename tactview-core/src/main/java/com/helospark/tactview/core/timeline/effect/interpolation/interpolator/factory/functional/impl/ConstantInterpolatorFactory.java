package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.impl;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.tactview.core.util.DesSerFactory;

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
    public ConstantInterpolator deserialize(Map<String, Object> data) {
        try {
            return objectMapper.readValue((String) data.get("instance"), ConstantInterpolator.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
