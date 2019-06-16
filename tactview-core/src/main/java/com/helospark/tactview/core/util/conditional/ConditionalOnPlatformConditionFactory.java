package com.helospark.tactview.core.util.conditional;

import java.lang.annotation.Annotation;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.conditional.AnnotationBasedConditionalProcessorFactory;
import com.helospark.lightdi.conditional.condition.DependencyCondition;
import com.helospark.lightdi.util.LightDiAnnotation;

@Component
public class ConditionalOnPlatformConditionFactory implements AnnotationBasedConditionalProcessorFactory {

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return ConditionalOnPlatform.class;
    }

    @Override
    public DependencyCondition getDependencyCondition(LightDiAnnotation annotation) {
        return new ConditionalOnPlatformCondition(annotation.getAttributeAs("value", TactviewPlatform[].class));
    }

}
