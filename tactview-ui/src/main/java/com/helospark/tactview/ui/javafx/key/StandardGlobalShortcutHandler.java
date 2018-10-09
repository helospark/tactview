package com.helospark.tactview.ui.javafx.key;

import java.util.function.Consumer;

import javafx.scene.input.KeyEvent;

public class StandardGlobalShortcutHandler implements GlobalShortcutHandler {
    private String name;
    private Consumer<KeyEvent> consumer;

    public StandardGlobalShortcutHandler(String name, Consumer<KeyEvent> consumer) {
        this.name = name;
        this.consumer = consumer;
    }

    @Override
    public void onShortcutExecuted(KeyEvent event) {
        consumer.accept(event);
    }

    @Override
    public String getOperationName() {
        return name;
    }

}
