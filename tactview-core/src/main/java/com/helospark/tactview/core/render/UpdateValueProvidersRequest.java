package com.helospark.tactview.core.render;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Generated;

import com.helospark.tactview.core.optionprovider.OptionProvider;

public class UpdateValueProvidersRequest {
    public String fileName;
    public Map<String, OptionProvider<?>> options;

    @Generated("SparkTools")
    private UpdateValueProvidersRequest(Builder builder) {
        this.fileName = builder.fileName;
        this.options = builder.options;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static Builder builderFrom(UpdateValueProvidersRequest updateValueProvidersRequest) {
        return new Builder(updateValueProvidersRequest);
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String fileName;
        private Map<String, OptionProvider<?>> options = Collections.emptyMap();

        private Builder() {
        }

        private Builder(UpdateValueProvidersRequest updateValueProvidersRequest) {
            this.fileName = updateValueProvidersRequest.fileName;
            this.options = updateValueProvidersRequest.options;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withOptions(Map<String, OptionProvider<?>> options) {
            this.options = options;
            return this;
        }

        public UpdateValueProvidersRequest build() {
            return new UpdateValueProvidersRequest(this);
        }
    }
}
