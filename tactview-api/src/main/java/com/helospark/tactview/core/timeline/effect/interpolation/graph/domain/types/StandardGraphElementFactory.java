package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types;

import java.util.function.Function;

import javax.annotation.Generated;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;

public class StandardGraphElementFactory implements GraphElementFactory {
    private String id;
    private String name;
    private GraphCategory category;
    private Function<GraphCreatorRequest, GraphElement> creator;
    private RestoreFunction restorer;
    private Function<String, Boolean> doesSupport;
    private boolean needsInputParam;

    @Generated("SparkTools")
    private StandardGraphElementFactory(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.category = builder.category;
        this.creator = builder.creator;
        this.restorer = builder.restorer;
        this.doesSupport = builder.doesSupport == null ? uri -> uri.equals(id) : builder.doesSupport;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return category.categoryString;
    }

    @Override
    public GraphElement createElement(GraphCreatorRequest request) {
        GraphElement result = creator.apply(request);
        result.setFactoryId(id);
        return result;
    }

    @Override
    public GraphElement restoreElement(JsonNode node, LoadMetadata metadata) {
        return restorer.restore(node, metadata);
    }

    @Override
    public boolean isNeedsInputParam() {
        return needsInputParam;
    }

    @Override
    public boolean doesSupport(String uri) {
        return doesSupport.apply(uri);
    }

    @FunctionalInterface
    static interface RestoreFunction {
        public GraphElement restore(JsonNode node, LoadMetadata metadata);
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private String id;
        private String name;
        private GraphCategory category;
        private Function<GraphCreatorRequest, GraphElement> creator;
        private RestoreFunction restorer;
        private Function<String, Boolean> doesSupport;
        private boolean needsInputParam;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withCategory(GraphCategory category) {
            this.category = category;
            return this;
        }

        public Builder withCreator(Function<GraphCreatorRequest, GraphElement> creator) {
            this.creator = creator;
            return this;
        }

        public Builder withRestorer(RestoreFunction restorer) {
            this.restorer = restorer;
            return this;
        }

        public Builder withDoesSupport(Function<String, Boolean> doesSupport) {
            this.doesSupport = doesSupport;
            return this;
        }

        public Builder withNeedsInputParam(boolean needsInputParam) {
            this.needsInputParam = needsInputParam;
            return this;
        }

        public StandardGraphElementFactory build() {
            return new StandardGraphElementFactory(this);
        }
    }

}
