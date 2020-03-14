package com.helospark.tactview.core.persistentstate;

import java.lang.reflect.Method;

import javax.annotation.Generated;

public class PersistentValueInfo {
    Object bean;
    String fieldName;
    Method getterMethod;
    Method setterMethod;
    PersistentState annotation;

    @Generated("SparkTools")
    private PersistentValueInfo(Builder builder) {
        this.bean = builder.bean;
        this.fieldName = builder.fieldName;
        this.getterMethod = builder.getterMethod;
        this.setterMethod = builder.setterMethod;
        this.annotation = builder.annotation;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private Object bean;
        private String fieldName;
        private Method getterMethod;
        private Method setterMethod;
        private PersistentState annotation;

        private Builder() {
        }

        public Builder withBean(Object bean) {
            this.bean = bean;
            return this;
        }

        public Builder withFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder withGetterMethod(Method getterMethod) {
            this.getterMethod = getterMethod;
            return this;
        }

        public Builder withSetterMethod(Method setterMethod) {
            this.setterMethod = setterMethod;
            return this;
        }

        public Builder withAnnotation(PersistentState annotation) {
            this.annotation = annotation;
            return this;
        }

        public PersistentValueInfo build() {
            return new PersistentValueInfo(this);
        }
    }
}
