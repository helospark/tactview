package com.helospark.tactview.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

public class ReflectionUtil {

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

}
