package com.helospark.tactview.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class ReflectionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtil.class);

    public static <T> void copyOrCloneFieldFromTo(T from, T to) {
        Arrays.stream(from.getClass().getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                .forEach(field -> copyField(to, from, field));
    }

    @SuppressWarnings("rawtypes")
    private static void copyField(Object to, Object from, Field fromField) {
        try {
            Field toField = to.getClass().getDeclaredField(fromField.getName());
            toField.setAccessible(true);
            fromField.setAccessible(true);

            Object fieldValue = fromField.get(from);
            if (fieldValue instanceof StatefulCloneable) {
                Object clonedValue = ((StatefulCloneable) fieldValue).deepClone();
                toField.set(to, clonedValue);
            } else {
                toField.set(to, fieldValue);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to clone", e);
        }
    }

    public static void collectSaveableFields(Object instance, Map<String, Object> saveableFields) {
        collectSaveableFieldsRecursively(instance, instance.getClass(), saveableFields);
    }

    private static void collectSaveableFieldsRecursively(Object instance, Class<? extends Object> clazz, Map<String, Object> saveableFields) {
        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> SavedContentAddable.class.isAssignableFrom(field.getType()))
                .forEach(field -> handleField(instance, saveableFields, field));
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            collectSaveableFieldsRecursively(instance, superClass, saveableFields);
        }
    }

    private static void handleField(Object instance, Map<String, Object> saveableFields, Field field) {
        try {
            field.setAccessible(true);
            SavedContentAddable<Object> value = (SavedContentAddable<Object>) field.get(instance);
            saveableFields.put(field.getName(), value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void realoadSavedFields(JsonNode jsonNode, Object instance) {
        realoadSaveableFieldsRecursively(instance, instance.getClass(), jsonNode);
    }

    private static void realoadSaveableFieldsRecursively(Object instance, Class<? extends Object> clazz, JsonNode jsonNode) {
        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> SavedContentAddable.class.isAssignableFrom(field.getType()))
                .forEach(field -> reloadField(instance, jsonNode, field));
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            realoadSaveableFieldsRecursively(instance, superClass, jsonNode);
        }
    }

    private static void reloadField(Object instance, JsonNode jsonNode, Field field) {
        try {
            String fieldName = field.getName();
            JsonNode nodeValue = jsonNode.get(fieldName);
            if (nodeValue == null) {
                LOGGER.warn("Unable to load field {}, using default value", fieldName);
            } else {
                field.setAccessible(true);
                Object newValue = deserialize(nodeValue, Object.class, (SavedContentAddable<?>) field.get(instance));
                field.set(instance, newValue);
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to load field {}", field.getName(), e);
        }
    }

    public static <T> T deserialize(JsonNode nodeValue, Class<T> toClass, SavedContentAddable<?> currentValue) {
        try {
            String deserializer = nodeValue.get("deserializer").textValue();

            DesSerFactory<Object> factory = (DesSerFactory<Object>) Class.forName(deserializer).newInstance(); // we could also get from field

            Object newValue = factory.deserialize(nodeValue, currentValue);
            return toClass.cast(newValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
