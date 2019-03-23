package com.helospark.tactview.ui.javafx.inputmode.strategy;

import javafx.scene.input.KeyCode;

public class StrategyKeyInput {
    private KeyCode keyEvent;

    public StrategyKeyInput(KeyCode keyEvent) {
        this.keyEvent = keyEvent;
    }

    public KeyCode getKeyCode() {
        return keyEvent;
    }

}
