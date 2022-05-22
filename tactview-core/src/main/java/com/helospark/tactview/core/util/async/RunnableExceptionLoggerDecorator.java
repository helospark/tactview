package com.helospark.tactview.core.util.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunnableExceptionLoggerDecorator implements Runnable {
    private Runnable runnable;
    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableExceptionLoggerDecorator.class);

    public RunnableExceptionLoggerDecorator(Runnable runnable) {
        this.runnable = runnable;
    }

    public static RunnableExceptionLoggerDecorator withExceptionLogging(Runnable runnable) {
        return new RunnableExceptionLoggerDecorator(runnable);
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (Exception e) {
            LOGGER.error("Exception while executing async", e);
            throw e;
        }
    }

}
