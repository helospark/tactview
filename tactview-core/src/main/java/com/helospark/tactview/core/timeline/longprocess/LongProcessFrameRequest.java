package com.helospark.tactview.core.timeline.longprocess;

import static com.helospark.tactview.core.timeline.longprocess.LongProcessDuplaceRequestStrategy.ONLY_KEEP_LATEST_REQUEST;

import javax.annotation.Generated;

public class LongProcessFrameRequest {
    private LongProcessDuplaceRequestStrategy duplaceRequestStrategy;

    @Generated("SparkTools")
    private LongProcessFrameRequest(Builder builder) {
        this.duplaceRequestStrategy = builder.duplaceRequestStrategy;
    }

    public LongProcessDuplaceRequestStrategy getDuplaceRequestStrategy() {
        return duplaceRequestStrategy;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private LongProcessDuplaceRequestStrategy duplaceRequestStrategy = ONLY_KEEP_LATEST_REQUEST;

        private Builder() {
        }

        public Builder withDuplaceRequestStrategy(LongProcessDuplaceRequestStrategy duplaceRequestStrategy) {
            this.duplaceRequestStrategy = duplaceRequestStrategy;
            return this;
        }

        public LongProcessFrameRequest build() {
            return new LongProcessFrameRequest(this);
        }
    }

}
