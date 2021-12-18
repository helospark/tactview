package com.helospark.tactview.core.util.cacheable;

import java.lang.reflect.Method;
import java.util.Map;

import com.github.benmanes.caffeine.cache.LoadingCache;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

public class CacheableProxy {
    private Object bean;
    Map<Method, LoadingCache<HashableArray, Object>> cacheables;

    public CacheableProxy(Object bean, Map<Method, LoadingCache<HashableArray, Object>> cacheables) {
        this.cacheables = cacheables;
        this.bean = bean;
    }

    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] args) throws Throwable {
        LoadingCache<HashableArray, Object> cacheLoader = cacheables.get(method);
        if (cacheLoader == null) {
            Method methodToProxyTo = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
            return methodToProxyTo.invoke(bean, args);
        } else {
            return cacheLoader.get(new HashableArray(args));
        }
    }
}
