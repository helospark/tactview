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

public class StringInterpolatorFactory implements DesSerFactory<StringInterpolator> {
    private ObjectMapper regularObjectMapper;

    public StringInterpolatorFactory() {
        regularObjectMapper = new ObjectMapper();
        regularObjectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        regularObjectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    @Override
    public void addDataForDeserialize(StringInterpolator instance, Map<String, Object> data) {
        try {
            data.put("instance", regularObjectMapper.writeValueAsString(instance));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StringInterpolator deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        try {
            StringInterpolator result = StaticObjectMapper.getterIgnoringOjectMapper.readValue(data.get("instance").asText(), StringInterpolator.class);
            result.defaultValue = ((StringInterpolator) currentFieldValue).defaultValue;
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
