package com.helospark.tactview.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class StaticObjectMapper {
    public static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(SavedContentAddable.class, new ItemSerializer());
        objectMapper.registerModule(module);
    }

    public static <T> T toValue(JsonNode node, String name, Class<T> type) {
        try {
            return objectMapper.treeToValue(node.get(name), type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
