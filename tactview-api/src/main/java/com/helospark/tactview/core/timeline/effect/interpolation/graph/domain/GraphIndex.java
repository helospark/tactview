package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphIndex {
    String id;

    public GraphIndex(@JsonProperty("id") String id) {
        this.id = id;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public static GraphIndex random() {
        return new GraphIndex(UUID.randomUUID().toString());
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof GraphIndex)) {
            return false;
        }
        GraphIndex castOther = (GraphIndex) other;
        return Objects.equals(id, castOther.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }

}
