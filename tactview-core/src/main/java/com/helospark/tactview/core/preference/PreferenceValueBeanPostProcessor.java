package com.helospark.tactview.core.preference;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.descriptor.DependencyDescriptor;
import com.helospark.lightdi.postprocessor.BeanPostProcessor;
import com.helospark.lightdi.util.AnnotationUtil;
import com.helospark.lightdi.util.LightDiAnnotation;

@Component
public class PreferenceValueBeanPostProcessor implements BeanPostProcessor {
    private Map<String, PreferenceValueData> preferenceValues = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, DependencyDescriptor dependencyDescriptor) {
        List<Method> methodsWithPreferenceValueAnnotation = AnnotationUtil.getMethodsWithAnnotation(bean.getClass(), PreferenceValue.class);

        for (var method : methodsWithPreferenceValueAnnotation) {
            LightDiAnnotation annotation = AnnotationUtil.getSingleAnnotationOfType(method, PreferenceValue.class);

            String name = annotation.getAttributeAs("name", String.class);
            String[] group = annotation.getAttributeAs("group", String[].class);

            String groupMerged = Arrays.stream(group).collect(Collectors.joining("."));

            name = groupMerged + "." + name;

            Parameter[] parameters = method.getParameters();
            if (parameters.length != 1) {
                throw new RuntimeException("PreferenceValue annotated method should have a single parameter");
            }
            Class<?> parameterType = parameters[0].getType();

            PreferenceValueData data = PreferenceValueData.builder()
                    .withBean(bean)
                    .withMethod(method)
                    .withName(name)
                    .withGroup(Arrays.asList(group))
                    .withDefaultValue(annotation.getAttributeAs("defaultValue", String.class))
                    .withType(parameterType)
                    .build();
            preferenceValues.put(name, data);
        }

        return bean;
    }

    public Map<String, PreferenceValueData> getPreferenceValues() {
        return preferenceValues;
    }

}
