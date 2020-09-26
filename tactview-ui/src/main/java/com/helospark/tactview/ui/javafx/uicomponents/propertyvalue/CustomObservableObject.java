package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.Objects;
import java.util.function.BiConsumer;

public class CustomObservableObject {
    private String previousValue;

    private BiConsumer<String, Boolean> listener;
    private BiConsumer<String, String> previousValueListener;

    public void registerListener(BiConsumer<String, Boolean> listener) {
        this.listener = listener;
    }

    public void registerPreviousValueListener(BiConsumer<String, String> previousValueListener) {
        this.previousValueListener = previousValueListener;
    }

    public void setValue(String value, boolean revertable) {
        if (!Objects.equals(previousValue, value) && this.listener != null) {
            this.listener.accept(value, revertable);
        }
        previousValue = value;
    }

    public void setValueWithRevertablePreviousValue(String newValue, String oldValue) {
        previousValueListener.accept(newValue, oldValue);
    }
}
