package com.helospark.tactview.core.util.async;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionLoggerDecorator<T> implements Supplier<T> {
    private Supplier<T> runnable;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLoggerDecorator.class);

    public ExceptionLoggerDecorator(Supplier<T> runnable) {
        this.runnable = runnable;
    }

    public static <T> ExceptionLoggerDecorator<T> withExceptionLogging(Supplier<T> runnable) {
        return new ExceptionLoggerDecorator<T>(runnable);
    }

    @Override
    public T get() {
        try {
            return runnable.get();
        } catch (Exception e) {
            LOGGER.error("Exception while executing async", e);
            throw e;
        }
    }

}
