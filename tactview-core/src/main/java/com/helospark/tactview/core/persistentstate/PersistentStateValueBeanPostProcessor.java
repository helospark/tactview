package com.helospark.tactview.core.persistentstate;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.lightdi.annotation.Value;
import com.helospark.lightdi.descriptor.DependencyDescriptor;
import com.helospark.lightdi.postprocessor.BeanPostProcessor;
import com.helospark.lightdi.util.AnnotationUtil;

@Component
public class PersistentStateValueBeanPostProcessor implements BeanPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistentStateValueBeanPostProcessor.class);
    private File persistentStateDirectory;
    private Map<String, List<PersistentValueInfo>> persistentValues = new ConcurrentHashMap<>();

    private ObjectMapper objectMapper;

    public PersistentStateValueBeanPostProcessor(@Value("${tactview.persistentStateDirectory}") String persistentStateDirectory,
            @Qualifier("simpleObjectMapper") ObjectMapper objectMapper) {
        this.persistentStateDirectory = new File(persistentStateDirectory);
        this.persistentStateDirectory.mkdirs();

        this.objectMapper = objectMapper;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, DependencyDescriptor dependencyDescriptor) {
        List<Field> annotatedFields = Arrays.stream(bean.getClass().getDeclaredFields())
                .filter(field -> AnnotationUtil.hasAnnotation(field, PersistentState.class))
                .collect(Collectors.toList());

        String preferenceName = bean.getClass().getName(); // make it configurable, so it could be used with backward compatibility
        List<PersistentValueInfo> infoList = new ArrayList<>();
        for (Field field : annotatedFields) {
            String name = field.getName();
            String upperCasedName = name.substring(0, 1).toUpperCase() + name.substring(1);
            String setterName = "set" + upperCasedName;
            String getterName = "get" + upperCasedName;

            Method getterMethod = getMethod(bean, getterName);
            Method setterMethod = getMethod(bean, setterName);

            Class<?> setterType = setterMethod.getParameterTypes()[0];

            if (setterMethod.getParameterTypes().length != 1
                    || getterMethod.getParameterTypes().length != 0
                    || !getterMethod.getReturnType().equals(setterType)) {
                throw new RuntimeException("Setter and getter (" + setterName + ", " + getterName + ") doesn't follow bean convention");
            }

            PersistentState annotation = (PersistentState) AnnotationUtil.getSingleAnnotationOfType(field, PersistentState.class).getType();

            PersistentValueInfo info = PersistentValueInfo.builder()
                    .withBean(bean)
                    .withAnnotation(annotation)
                    .withFieldName(name)
                    .withGetterMethod(getterMethod)
                    .withSetterMethod(setterMethod)
                    .build();

            infoList.add(info);

            Optional<Object> savedPreference = readSavedPeference(preferenceName, info);

            if (savedPreference.isPresent()) {
                setPersistentValue(bean, info, savedPreference);
            }

        }
        if (!infoList.isEmpty()) {
            persistentValues.put(preferenceName, infoList);
        }

        return bean;
    }

    @PreDestroy
    public void preDestroy() {
        persistentValues.entrySet()
                .stream()
                .forEach(entry -> saveEntry(entry));
    }

    private void saveEntry(Entry<String, List<PersistentValueInfo>> entry) {
        Map<String, Object> mapToSave = createMapFor(entry.getValue());
        if (mapToSave.size() > 0) {
            File outputFile = new File(persistentStateDirectory, entry.getKey() + ".json");
            try {
                objectMapper.writeValue(outputFile, mapToSave);
            } catch (Exception e) {
                LOGGER.warn("Error while saving preference for " + entry.getKey(), e);
            }
        }
    }

    private Map<String, Object> createMapFor(List<PersistentValueInfo> list) {
        Map<String, Object> result = new HashMap<>();

        for (var element : list) {
            try {
                Object value = element.getterMethod.invoke(element.bean);
                result.put(element.fieldName, value);
            } catch (Exception e) {
                LOGGER.warn("Unable to get preference value " + element.fieldName);
            }

        }

        return result;
    }

    private void setPersistentValue(Object bean, PersistentValueInfo info, Optional<Object> savedPreference) {
        try {
            info.setterMethod.invoke(bean, savedPreference.get());
        } catch (Exception e) {
            LOGGER.warn("Unable to set persistent state", e);
        }
    }

    private Optional<Object> readSavedPeference(String preferenceName, PersistentValueInfo info) {
        Optional<Object> result = Optional.empty();
        File location = new File(persistentStateDirectory, preferenceName + ".json");
        if (location.exists()) {
            try {
                JsonNode tree = objectMapper.readTree(location);
                if (tree.has(info.fieldName)) {
                    result = Optional.ofNullable(objectMapper.treeToValue(tree.get(info.fieldName), info.setterMethod.getParameterTypes()[0]));
                }

            } catch (Exception e) {
                LOGGER.warn("Unable to read persistent state", e);
            }
        }
        return result;
    }

    private Method getMethod(Object bean, String methodName) {
        Method method = Arrays.stream(bean.getClass().getDeclaredMethods())
                .filter(a -> a.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No method named " + methodName + " found on class " + bean.getClass().getName() + " but it is required by PersistentState"));
        method.setAccessible(true);
        return method;
    }

}
