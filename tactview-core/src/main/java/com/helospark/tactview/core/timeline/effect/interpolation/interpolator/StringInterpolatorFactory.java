package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.tactview.core.util.DesSerFactory;

public class StringInterpolatorFactory implements DesSerFactory<StringInterpolator> {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void addDataForDeserialize(StringInterpolator instance, Map<String, Object> data) {
        try {
            data.put("instance", objectMapper.writeValueAsString(instance));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StringInterpolator deserialize(Map<String, Object> data) {
        try {
            return objectMapper.readValue((String) data.get("instance"), StringInterpolator.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
