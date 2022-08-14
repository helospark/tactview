package com.helospark.tactview.core;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.ComponentScan;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.PropertySource;
import com.helospark.lightdi.annotation.Value;

@Configuration
@ComponentScan
@PropertySource("classpath:application.properties")
@PropertySource(value = "classpath:application-${tactview.profile:}.properties", order = -1, ignoreResourceNotFound = true)
public class TactViewCoreConfiguration {

    @Bean
    public ObjectMapper simpleObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        objectMapper.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
        return objectMapper;
    }

    @Bean
    public ObjectMapper prettyPrintingObjectMapper() {
        ObjectMapper result = new ObjectMapper();
        result.enable(SerializationFeature.INDENT_OUTPUT);
        result.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        result.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
        return result;
    }

    @Bean
    public ObjectMapper getterIgnoringObjectMapper() {
        ObjectMapper regularObjectMapper = new ObjectMapper();
        regularObjectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        regularObjectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        regularObjectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        regularObjectMapper.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
        return regularObjectMapper;
    }

    @Bean
    public ScheduledExecutorService generalTaskScheduledService(@Value("${scheduledexecutor.threads}") int threads) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("scheduled-job-executors-%d").build();
        return new ScheduledThreadPoolExecutor(threads, namedThreadFactory);
    }

    @Bean
    public ThreadPoolExecutor longRunningTaskExecutorService() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("longrunning-task-executors-%d").build();

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (availableProcessors <= 0) {
            availableProcessors = 1;
        }

        return new ThreadPoolExecutor(0, availableProcessors, 5000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100), namedThreadFactory);
    }

    @Bean
    public ThreadPoolExecutor prefetchThreadPoolExecutorService(@Value("${prefetch.threads}") int threads) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("prefetch-job-executors-%d").build();
        return new ThreadPoolExecutor(0, threads, 100000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100), namedThreadFactory);
    }

}
