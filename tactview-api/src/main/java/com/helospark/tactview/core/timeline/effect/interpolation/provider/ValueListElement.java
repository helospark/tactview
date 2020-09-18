package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Objects;

public class ValueListElement {
    private final String id;
    private final String text;

    public ValueListElement(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return id; // id must be returned for UI to work
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ValueListElement)) {
            return false;
        }
        ValueListElement castOther = (ValueListElement) other;
        return Objects.equals(id, castOther.id) && Objects.equals(text, castOther.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text);
    }
}
