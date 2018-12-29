package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.Objects;
import java.util.function.Consumer;

public class CustomObservableObject {
    private String previousValue;

    private Consumer<String> listener;

    public void registerListener(Consumer<String> listener) {
        this.listener = listener;
    }

    public void setValue(String value) {
        if (!Objects.equals(previousValue, value) && this.listener != null) {
            this.listener.accept(value);
        }
        previousValue = value;
    }
}
