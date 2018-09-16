package com.helospark.tactview.core.util.jpaplugin;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Generated;

import com.helospark.lightdi.conditional.condition.DependencyCondition;
import com.helospark.lightdi.descriptor.DependencyDescriptor;

public class JnaDependencyDefinition extends DependencyDescriptor {
    private String backingLibrary;

    public String getBackingLibrary() {
        return backingLibrary;
    }

    @Generated("SparkTools")
    private JnaDependencyDefinition(Builder builder) {
        this.clazz = builder.clazz;
        this.qualifier = builder.qualifier;
        this.scope = builder.scope;
        this.isLazy = builder.isLazy;
        this.isPrimary = builder.isPrimary;
        this.order = builder.order;
        this.postConstructMethods = builder.postConstructMethods;
        this.preDestroyMethods = builder.preDestroyMethods;
        this.conditions = builder.conditions;
        this.initalizationFinished = builder.initalizationFinished;
        this.importingClass = builder.importingClass;
        this.backingLibrary = builder.backingLibrary;
        super.setPrimary(builder.primary);
        super.setLazy(builder.lazy);
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private Class<?> clazz;
        private String qualifier;
        private String scope;
        private boolean isLazy;
        private boolean isPrimary;
        private int order;
        private List<Method> postConstructMethods = Collections.emptyList();
        private List<Method> preDestroyMethods = Collections.emptyList();
        private List<DependencyCondition> conditions = Collections.emptyList();
        private boolean initalizationFinished;
        private Optional<Class<?>> importingClass = Optional.empty();
        private String backingLibrary;
        private boolean primary;
        private boolean lazy;

        private Builder() {
        }

        public Builder withClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder withQualifier(String qualifier) {
            this.qualifier = qualifier;
            return this;
        }

        public Builder withScope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder withIsLazy(boolean isLazy) {
            this.isLazy = isLazy;
            return this;
        }

        public Builder withIsPrimary(boolean isPrimary) {
            this.isPrimary = isPrimary;
            return this;
        }

        public Builder withOrder(int order) {
            this.order = order;
            return this;
        }

        public Builder withPostConstructMethods(List<Method> postConstructMethods) {
            this.postConstructMethods = postConstructMethods;
            return this;
        }

        public Builder withPreDestroyMethods(List<Method> preDestroyMethods) {
            this.preDestroyMethods = preDestroyMethods;
            return this;
        }

        public Builder withConditions(List<DependencyCondition> conditions) {
            this.conditions = conditions;
            return this;
        }

        public Builder withInitalizationFinished(boolean initalizationFinished) {
            this.initalizationFinished = initalizationFinished;
            return this;
        }

        public Builder withImportingClass(Optional<Class<?>> importingClass) {
            this.importingClass = importingClass;
            return this;
        }

        public Builder withBackingLibrary(String backingLibrary) {
            this.backingLibrary = backingLibrary;
            return this;
        }

        public Builder withPrimary(boolean primary) {
            this.primary = primary;
            return this;
        }

        public Builder withLazy(boolean lazy) {
            this.lazy = lazy;
            return this;
        }

        public JnaDependencyDefinition build() {
            return new JnaDependencyDefinition(this);
        }
    }

}
