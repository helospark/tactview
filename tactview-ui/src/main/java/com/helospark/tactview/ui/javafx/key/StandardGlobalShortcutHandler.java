package com.helospark.tactview.ui.javafx.key;

import java.util.function.Consumer;

public class StandardGlobalShortcutHandler implements GlobalShortcutHandler {
    private String name;
    private Consumer<ShortcutExecutedEvent> consumer;

    public StandardGlobalShortcutHandler(String name, Consumer<ShortcutExecutedEvent> consumer) {
        this.name = name;
        this.consumer = consumer;
    }

    @Override
    public void onShortcutExecuted(ShortcutExecutedEvent event) {
        consumer.accept(event);
    }

    @Override
    public String getOperationName() {
        return name;
    }

}
