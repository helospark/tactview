package com.helospark.tactview.core.timeline.effect.interpolation.interpolator;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;
import com.helospark.tactview.core.util.StaticObjectMapper;

public class StepStringInterpolatorFactory implements DesSerFactory<StepStringInterpolator> {
    private ObjectMapper regularObjectMapper;

    public StepStringInterpolatorFactory() {
        regularObjectMapper = new ObjectMapper();
        regularObjectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        regularObjectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    @Override
    public void addDataForDeserialize(StepStringInterpolator instance, Map<String, Object> data) {
        try {
            data.put("instance", regularObjectMapper.writeValueAsString(instance));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StepStringInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            return StaticObjectMapper.getterIgnoringOjectMapper.readValue(data.get("instance").asText(), StepStringInterpolator.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
