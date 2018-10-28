package com.helospark.tactview.core.util;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Generated;

public class SimplePixelTransformerRequest {
    public int x;
    public int y;
    public int[] input;
    public int[] output;
    private Map<ThreadLocalProvider<?>, Object> threadLocals;

    @Generated("SparkTools")
    private SimplePixelTransformerRequest(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.input = builder.input;
        this.output = builder.output;
        this.threadLocals = builder.threadLocals;
    }

    public <T> T getThreadLocal(ThreadLocalProvider<T> clazz) {
        return (T) threadLocals.get(clazz);
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private int x;
        private int y;
        private int[] input;
        private int[] output;
        private Map<ThreadLocalProvider<?>, Object> threadLocals = Collections.emptyMap();

        private Builder() {
        }

        public Builder withx(int x) {
            this.x = x;
            return this;
        }

        public Builder withy(int y) {
            this.y = y;
            return this;
        }

        public Builder withInput(int[] input) {
            this.input = input;
            return this;
        }

        public Builder withOutput(int[] output) {
            this.output = output;
            return this;
        }

        public Builder withThreadLocals(Map<ThreadLocalProvider<?>, Object> threadLocals) {
            this.threadLocals = threadLocals;
            return this;
        }

        public SimplePixelTransformerRequest build() {
            return new SimplePixelTransformerRequest(this);
        }
    }
}
