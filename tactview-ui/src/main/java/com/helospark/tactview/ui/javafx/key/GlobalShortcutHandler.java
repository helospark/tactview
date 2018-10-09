package com.helospark.tactview.ui.javafx.key;

import javafx.scene.input.KeyEvent;

public interface GlobalShortcutHandler {
    public void onShortcutExecuted(KeyEvent event);

    public String getOperationName();
}
