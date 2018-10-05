package com.helospark.tactview.core.util.logger;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.descriptor.DependencyDescriptor;
import com.helospark.lightdi.postprocessor.BeanPostProcessor;
import com.helospark.lightdi.util.AnnotationUtil;

@Component
public class Slf4jBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, DependencyDescriptor dependencyDescriptor) {
        Arrays.asList(bean.getClass().getDeclaredFields())
                .stream()
                .filter(field -> AnnotationUtil.hasAnnotation(field, Slf4j.class))
                .forEach(field -> {
                    if (!(field.getType().isAssignableFrom(Logger.class))) {
                        throw new RuntimeException("Field should by type of slf4j logger");
                    }
                    try {
                        field.setAccessible(true);
                        field.set(bean, LoggerFactory.getLogger(bean.getClass()));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
        return bean;
    }

}
