package com.helospark.tactview.core.util.jpaplugin;

import java.util.Collections;
import java.util.List;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.beanfactory.chain.BeanFactoryChainItem;
import com.helospark.lightdi.descriptor.DependencyDescriptor;
import com.sun.jna.Native;

public class JnaBeanFactoryChainItem implements BeanFactoryChainItem {

    @Override
    public Object createBean(LightDiContext lightDiContext, DependencyDescriptor dependencyToCreate) throws Exception {
        Class<?> clazz = dependencyToCreate.getClazz();
        String libraryName = ((JnaDependencyDefinition) dependencyToCreate).getBackingLibrary();
        return Native.loadLibrary(libraryName, clazz);
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
