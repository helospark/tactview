package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphConnectionDescriptor {
    String name;
    GraphAcceptType acceptType;

    @Override
    public int hashCode() {
        return Objects.hash(name, acceptType);
    }

    public GraphConnectionDescriptor(@JsonProperty("name") String name, @JsonProperty("acceptType") GraphAcceptType acceptType) {
        this.name = name;
        this.acceptType = acceptType;
    }

    public String getName() {
        return name;
    }

    public GraphAcceptType getAcceptType() {
        return acceptType;
    }

    @Override
    public String toString() {
        return "GraphConnectionDescriptor [name=" + name + ", acceptType=" + acceptType + "]";
    }

}
