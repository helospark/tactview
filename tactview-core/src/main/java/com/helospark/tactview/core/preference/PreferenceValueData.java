package com.helospark.tactview.core.preference;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

public class PreferenceValueData {
    public Method method;
    public Object bean;
    public String name;
    public List<String> group;
    public String defaultValue;
    public Class<?> type;

    @Generated("SparkTools")
    private PreferenceValueData(Builder builder) {
        this.method = builder.method;
        this.bean = builder.bean;
        this.name = builder.name;
        this.group = builder.group;
        this.defaultValue = builder.defaultValue;
        this.type = builder.type;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private Method method;
        private Object bean;
        private String name;
        private List<String> group = Collections.emptyList();
        private String defaultValue;
        private Class<?> type;

        private Builder() {
        }

        public Builder withMethod(Method method) {
            this.method = method;
            return this;
        }

        public Builder withBean(Object bean) {
            this.bean = bean;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withGroup(List<String> group) {
            this.group = group;
            return this;
        }

        public Builder withDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder withType(Class<?> type) {
            this.type = type;
            return this;
        }

        public PreferenceValueData build() {
            return new PreferenceValueData(this);
        }
    }
}
