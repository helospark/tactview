package com.helospark.tactview.core.util.jpaplugin;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.beanfactory.chain.BeanFactoryChainItem;
import com.helospark.lightdi.descriptor.DependencyDescriptor;
import com.sun.jna.Native;

public class JnaBeanFactoryChainItem implements BeanFactoryChainItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(JnaBeanFactoryChainItem.class);

    @Override
    public Object createBean(LightDiContext lightDiContext, DependencyDescriptor dependencyToCreate) throws Exception {
        Class<?> clazz = dependencyToCreate.getClazz();
        String libraryName = ((JnaDependencyDefinition) dependencyToCreate).getBackingLibrary();
        try {
            return Native.loadLibrary(libraryName, clazz);
        } catch (UnsatisfiedLinkError e) {
            LOGGER.error("Unable to load library {}", libraryName);
            throw e;
        }
    }

    @Override
    public boolean isSupported(DependencyDescriptor dependencyDescriptor) {
        return dependencyDescriptor instanceof JnaDependencyDefinition;
    }

    @Override
    public List<DependencyDescriptor> extractDependencies(DependencyDescriptor dependencyToCreate) {
        return Collections.emptyList();
    }

}
