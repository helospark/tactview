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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

/**
 * Adds proxy around {@literal @}Cacheable annotated methods.
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

    // Magic explanation: https://brixomatic.wordpress.com/2012/12/22/dynamic-proxies-for-classes/
    private Object createProxyCacheableProxyAround(Object bean, List<Method> cacheableMethods) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(bean.getClass());
        enhancer.setCallbackType(CacheableProxy.class);
        enhancer.setUseCache(false);

        final Class<?> proxyClass = enhancer.createClass();
        Enhancer.registerStaticCallbacks(proxyClass, new Callback[]{createCacheableInterceptor(bean, cacheableMethods)});
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

            LoadingCache<HashableArray, Object> cache = coffeinCacheBuilder
                    .build(key -> method.invoke(bean, key.getElements()));
            cacheables.put(method, cache);
        }
        return new CacheableProxy(bean, cacheables);
    }

}
