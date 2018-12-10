package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;
import com.helospark.tactview.core.util.StaticObjectMapper;

public class StringInterpolatorFactory implements DesSerFactory<StringInterpolator> {

    @Override
    public void addDataForDeserialize(StringInterpolator instance, Map<String, Object> data) {
        try {
            data.put("instance", StaticObjectMapper.objectMapper.writeValueAsString(instance));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StringInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue) {
        try {
            return StaticObjectMapper.objectMapper.readValue(data.get("instance").asText(), StringInterpolator.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
