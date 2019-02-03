package com.helospark.tactview.ui.javafx.key;

import javafx.scene.input.KeyCombination;

public class ShortcutExecutedEvent {
    private KeyCombination keyCodeCombination;

    public ShortcutExecutedEvent(KeyCombination keyCodeCombination) {
        this.keyCodeCombination = keyCodeCombination;
    }

    public KeyCombination getKeyCodeCombination() {
        return keyCodeCombination;
    }

}
