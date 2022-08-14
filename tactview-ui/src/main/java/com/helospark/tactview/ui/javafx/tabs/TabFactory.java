package com.helospark.tactview.ui.javafx.tabs;

import javafx.scene.control.Tab;

public interface TabFactory {

    public Tab createTabContent();

    default boolean isValueProvider() {
        return false;
    }

}
