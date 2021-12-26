package com.helospark.tactview.core.util.cacheable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.objenesis.ObjenesisHelper;

import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.descriptor.DependencyDescriptor;
import com.helospark.lightdi.postprocessor.BeanPostProcessor;
import com.helospark.lightdi.util.AnnotationUtil;
import com.helospark.lightdi.util.LightDiAnnotation;
import com.helospark.lightdi.util.ReflectionUtil;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Adds proxy around {@literal @}Cacheable annotated methods.
 *
 * @author helospark
 */
@Component
public class CacheableBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, DependencyDescriptor dependencyDescriptor) {
        List<Method> cacheableMethods = ReflectionUtil.getNonObjectMethods(bean.getClass())
                .filter(method -> AnnotationUtil.doesElementContainAnyAnnotationOf(method, Cacheable.class))
                .collect(Collectors.toList());
        if (cacheableMethods.isEmpty()) {
            return bean;
        } else {
            return createProxyCacheableProxyAround(bean, cacheableMethods);
        }
    }

    private Object createProxyCacheableProxyAround(Object bean, List<Method> cacheableMethods) {
        Class<?> proxyClass = new ByteBuddy()
                .subclass(bean.getClass())
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(createCacheableInterceptor(bean, cacheableMethods)))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        return ObjenesisHelper.newInstance(proxyClass);
    }

    private CacheableProxy createCacheableInterceptor(Object bean, List<Method> cacheableMethods) {
        Map<Method, LoadingCache<HashableArray, Object>> cacheables = new HashMap<>(cacheableMethods.size() * 2);
        for (Method method : cacheableMethods) {
            LightDiAnnotation annotation = AnnotationUtil.getSingleAnnotationOfType(method, Cacheable.class);
            Class<?> cacheValue = method.getReturnType();

            Caffeine<Object, Object> coffeinCacheBuilder = Caffeine.newBuilder()
                    .maximumSize(annotation.getAttributeAs("size", Integer.class))
                    .expireAfterWrite(annotation.getAttributeAs("cacheTimeInMilliseconds", Integer.class), TimeUnit.MILLISECONDS);

            if (CacheCleanable.class.isAssignableFrom(cacheValue)) {
                coffeinCacheBuilder.writer(new CacheWriter<Object, Object>() {

                    @Override
                    public void write(Object key, Object value) {

                    }

                    @Override
                    public void delete(Object key, Object value, RemovalCause cause) {
                        ((CacheCleanable) value).clean();
                    }
                });
            }

            LoadingCache<HashableArray, Object> cache = coffeinCacheBuilder.build(key -> method.invoke(bean, key.getElements()));
            cacheables.put(method, cache);
        }
        return new CacheableProxy(bean, cacheables);
    }

}
