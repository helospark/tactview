package com.helospark.lightdi.it.plugintest.crudrepository;

import static com.helospark.lightdi.util.BeanNameGenerator.createBeanNameForStereotypeAnnotatedClass;

import java.util.Collections;
import java.util.List;

import com.helospark.lightdi.conditional.ConditionalAnnotationsExtractor;
import com.helospark.lightdi.definitioncollector.BeanDefinitionCollectorChainItem;
import com.helospark.lightdi.descriptor.DependencyDescriptor;
import com.helospark.lightdi.util.AnnotationUtil;
import com.helospark.lightdi.util.IsLazyExtractor;
import com.helospark.lightdi.util.IsPrimaryExtractor;
import com.helospark.lightdi.util.QualifierExtractor;

public class CrudRepositoryBeanDefinitionCollectorChainItem implements BeanDefinitionCollectorChainItem {
    private ConditionalAnnotationsExtractor conditionalExtractor;

    @Override
    public List<DependencyDescriptor> collectDefinitions(Class<?> clazz) {
        if (AnnotationUtil.hasAnnotation(clazz, CrudRepository.class)) {
            return Collections.singletonList(CrudRepositoryDefinition.builder()
                    .withClazz(clazz)
                    .withQualifier(createBeanNameForStereotypeAnnotatedClass(clazz))
                    .withScope(QualifierExtractor.extractScope(clazz))
                    .withIsLazy(IsLazyExtractor.isLazy(clazz))
                    .withIsPrimary(IsPrimaryExtractor.isPrimary(clazz))
                    .withConditions(conditionalExtractor.extractConditions(clazz))
                    .build());
        }
        return Collections.emptyList();
    }

    /**
     * Unfortunately when this class created, the below dependency doesn't yet exists, so we have to set after
     * the internal DI is initialized.
     */
    public void setConditionalExtractor(ConditionalAnnotationsExtractor conditionalExtractor) {
        this.conditionalExtractor = conditionalExtractor;
    }
}
