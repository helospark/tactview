package com.helospark.tactview.core.util.cacheable;

import java.lang.reflect.Method;
import java.util.Map;

import com.github.benmanes.caffeine.cache.LoadingCache;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CacheableProxy implements MethodInterceptor {
    private Object bean;
    Map<Method, LoadingCache<HashableArray, Object>> cacheables;

    public CacheableProxy(Object bean, Map<Method, LoadingCache<HashableArray, Object>> cacheables) {
        this.cacheables = cacheables;
        this.bean = bean;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        LoadingCache<HashableArray, Object> cacheLoader = cacheables.get(method);
        if (cacheLoader == null) {
            Method methodToProxyTo = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
            return methodToProxyTo.invoke(bean, args);
        } else {
            return cacheLoader.get(new HashableArray(args));
        }
    }
}