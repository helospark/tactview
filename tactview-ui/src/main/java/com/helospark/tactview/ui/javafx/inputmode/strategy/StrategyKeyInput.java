package com.helospark.tactview.ui.javafx.inputmode.strategy;

import javafx.scene.input.KeyEvent;

public class StrategyKeyInput {
    private KeyEvent keyEvent;

    public StrategyKeyInput(KeyEvent keyEvent) {
        this.keyEvent = keyEvent;
    }

    public KeyEvent getKeyEvent() {
        return keyEvent;
    }

}
