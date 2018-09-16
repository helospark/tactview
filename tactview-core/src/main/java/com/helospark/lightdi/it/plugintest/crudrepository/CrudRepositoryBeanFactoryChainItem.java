package com.helospark.lightdi.it.plugintest.crudrepository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.beanfactory.chain.BeanFactoryChainItem;
import com.helospark.lightdi.descriptor.DependencyDescriptor;

public class CrudRepositoryBeanFactoryChainItem implements BeanFactoryChainItem {

    @Override
    public Object createBean(LightDiContext lightDiContext, DependencyDescriptor dependencyToCreate) throws Exception {
        Class<?> clazz = dependencyToCreate.getClazz();
        return createDynamicProxy(clazz);
    }

    @Override
    public boolean isSupported(DependencyDescriptor dependencyDescriptor) {
        return dependencyDescriptor instanceof CrudRepositoryDefinition;
    }

    @Override
    public List<DependencyDescriptor> extractDependencies(DependencyDescriptor dependencyToCreate) {
        return Collections.emptyList();
    }


    // Imagine some cool dynamic proxy generation, unlike the next:

    private Object createDynamicProxy(Class<?> clazz) {
        return Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[] { clazz },
                new DynamicInvocationHandler());
    }

    class DynamicInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            return "Database access result";
        }
    }
}
