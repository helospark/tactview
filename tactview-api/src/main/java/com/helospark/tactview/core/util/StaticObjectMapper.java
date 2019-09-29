package com.helospark.tactview.core.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.deserializer.TimelinePositionMapDeserializer;

public class StaticObjectMapper {
    public static ObjectMapper objectMapper;
    public static ObjectMapper getterIgnoringOjectMapper;

    static {
        objectMapper = setupObjectMapper();
        getterIgnoringOjectMapper = setupObjectMapper();
        getterIgnoringOjectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        getterIgnoringOjectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    private static ObjectMapper setupObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(SavedContentAddable.class, new ItemSerializer());
        module.addKeyDeserializer(TimelinePosition.class, new TimelinePositionMapDeserializer());
        objectMapper.registerModule(module);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    public static <T> T toValue(JsonNode node, LoadMetadata loadMetadata, String name, Class<T> type) {
        try {
            return objectMapper.treeToValue(node.get(name), type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
