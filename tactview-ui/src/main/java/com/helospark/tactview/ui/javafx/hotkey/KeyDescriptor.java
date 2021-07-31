package com.helospark.tactview.ui.javafx.hotkey;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import javafx.scene.input.KeyCodeCombination;

public class KeyDescriptor {
    private String name;
    private KeyCodeCombination combination;

    public KeyDescriptor(@JsonProperty("name") String name, @JsonProperty("combination") KeyCodeCombination combination) {
        this.name = name;
        this.combination = combination;
    }

    public String getName() {
        return name;
    }

    public KeyCodeCombination getCombination() {
        return combination;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof KeyDescriptor)) {
            return false;
        }
        KeyDescriptor castOther = (KeyDescriptor) other;
        return Objects.equals(name, castOther.name) && Objects.equals(combination, castOther.combination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, combination);
    }

    @Override
    public String toString() {
        return "KeyDescriptor [name=" + name + ", combination=" + combination + "]";
    }

    public KeyDescriptor butWithCombination(KeyCodeCombination combination2) {
        return new KeyDescriptor(name, combination2);
    }

}
