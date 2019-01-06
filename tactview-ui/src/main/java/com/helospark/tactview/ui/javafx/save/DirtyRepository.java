package com.helospark.tactview.ui.javafx.save;

import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;

@Component
public class DirtyRepository {
    private SimpleBooleanProperty dirty = new SimpleBooleanProperty();

    public boolean isDirty() {
        return dirty.get();
    }

    public void setDirty(boolean dirty) {
        this.dirty.set(dirty);
    }

    public void addUiChangeListener(Consumer<Boolean> onDirtyChangeListener) {
        dirty.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> onDirtyChangeListener.accept(newValue));
        });
    }

}
