package com.helospark.tactview.core.util;

import javax.annotation.Generated;

public class SimplePixelTransformerRequest {
    public int x;
    public int y;
    public int[] input;
    public int[] output;

    @Generated("SparkTools")
    private SimplePixelTransformerRequest(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.input = builder.originalPixelComponents;
        this.output = builder.responsePixelComponents;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private int x;
        private int y;
        private int[] originalPixelComponents;
        private int[] responsePixelComponents;

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

        public Builder withOriginalPixelComponents(int[] originalPixelComponents) {
            this.originalPixelComponents = originalPixelComponents;
            return this;
        }

        public Builder withResponsePixelComponents(int[] responsePixelComponents) {
            this.responsePixelComponents = responsePixelComponents;
            return this;
        }

        public SimplePixelTransformerRequest build() {
            return new SimplePixelTransformerRequest(this);
        }
    }
}
