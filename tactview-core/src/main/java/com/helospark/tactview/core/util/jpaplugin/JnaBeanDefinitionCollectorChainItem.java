package com.helospark.tactview.core.util.jpaplugin;

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
import com.sun.jna.Library;

public class JnaBeanDefinitionCollectorChainItem implements BeanDefinitionCollectorChainItem {
    private ConditionalAnnotationsExtractor conditionalExtractor;

    @Override
    public List<DependencyDescriptor> collectDefinitions(Class<?> clazz) {
        if (AnnotationUtil.hasAnnotation(clazz, NativeImplementation.class)) {
            if (!Library.class.isAssignableFrom(clazz)) {
                // Maybe we could generate this during runtime as well, however for now, let's just make the developer deal with the problem
                throw new RuntimeException(clazz + " should implement " + Library.class.getName());
            }
            String backingLibrary = AnnotationUtil.getSingleAnnotationOfType(clazz, NativeImplementation.class).getAttributeAs("value", String.class);
            return Collections.singletonList(JnaDependencyDefinition.builder()
                    .withClazz(clazz)
                    .withQualifier(createBeanNameForStereotypeAnnotatedClass(clazz))
                    .withScope(QualifierExtractor.extractScope(clazz))
                    .withIsLazy(IsLazyExtractor.isLazy(clazz))
                    .withIsPrimary(IsPrimaryExtractor.isPrimary(clazz))
                    .withConditions(conditionalExtractor.extractConditions(clazz))
                    .withMergedAnnotations(AnnotationUtil.getAllMergedAnnotations(clazz))
                    .withBackingLibrary(backingLibrary)
                    .build());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Unfortunately when this class created, the below dependency doesn't yet exists, so we have to set after
     * the internal DI is initialized.
     */
    public void setConditionalExtractor(ConditionalAnnotationsExtractor conditionalExtractor) {
        this.conditionalExtractor = conditionalExtractor;
    }
}
