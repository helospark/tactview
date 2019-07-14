package com.helospark.tactview.core.util.cacheable;

import java.util.Arrays;

public class HashableArray {
    private Object[] elements;

    public HashableArray(Object[] elements) {
        this.elements = elements;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HashableArray)) {
            return false;
        }
        HashableArray castOther = (HashableArray) other;
        return Arrays.deepEquals(elements, castOther.elements);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(elements);
    }

    @Override
    public String toString() {
        return "HasheableArray [elements=" + Arrays.toString(elements) + "]";
    }

    public Object[] getElements() {
        return elements;
    }

}
