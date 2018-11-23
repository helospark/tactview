package com.helospark.tactview.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

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

}
