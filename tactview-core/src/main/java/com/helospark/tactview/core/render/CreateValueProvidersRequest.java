package com.helospark.tactview.core.render;

import javax.annotation.Generated;

public class CreateValueProvidersRequest {
    public String fileName;

    @Generated("SparkTools")
    private CreateValueProvidersRequest(Builder builder) {
        this.fileName = builder.fileName;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static Builder builderFrom(CreateValueProvidersRequest createValueProvidersRequest) {
        return new Builder(createValueProvidersRequest);
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String fileName;

        private Builder() {
        }

        private Builder(CreateValueProvidersRequest createValueProvidersRequest) {
            this.fileName = createValueProvidersRequest.fileName;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public CreateValueProvidersRequest build() {
            return new CreateValueProvidersRequest(this);
        }
    }
}
