package com.helospark.tactview.ui.javafx.key;

public interface GlobalShortcutHandler {
    public void onShortcutExecuted(ShortcutExecutedEvent event);

    public String getOperationName();
}
