package com.helospark.tactview.ui.javafx.key;

import java.util.function.Consumer;

public class StandardGlobalShortcutHandler implements GlobalShortcutHandler {
    private Consumer<ShortcutExecutedEvent> consumer;

    public StandardGlobalShortcutHandler(Consumer<ShortcutExecutedEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onShortcutExecuted(ShortcutExecutedEvent event) {
        consumer.accept(event);
    }

}
