package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectionIndex {
    String id;

    public ConnectionIndex(@JsonProperty("id") String id) {
        this.id = id;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public static ConnectionIndex random() {
        return new ConnectionIndex(UUID.randomUUID().toString());
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ConnectionIndex)) {
            return false;
        }
        ConnectionIndex castOther = (ConnectionIndex) other;
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
