package com.helospark.tactview.core.timeline;

import javax.annotation.Generated;

public class MoveChannelRequest {
    private int originalIndex;
    private int newIndex;

    public int getOriginalIndex() {
        return originalIndex;
    }

    public int getNewIndex() {
        return newIndex;
    }

    @Generated("SparkTools")
    private MoveChannelRequest(Builder builder) {
        this.originalIndex = builder.originalIndex;
        this.newIndex = builder.newIndex;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private int originalIndex;
        private int newIndex;

        private Builder() {
        }

        public Builder withOriginalIndex(int originalIndex) {
            this.originalIndex = originalIndex;
            return this;
        }

        public Builder withNewIndex(int newIndex) {
            this.newIndex = newIndex;
            return this;
        }

        public MoveChannelRequest build() {
            return new MoveChannelRequest(this);
        }
    }

}
