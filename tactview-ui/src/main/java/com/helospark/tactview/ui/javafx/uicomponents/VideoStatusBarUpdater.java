package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.lightdi.annotation.Component;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Component
public class VideoStatusBarUpdater {
    private StringProperty text = new SimpleStringProperty();

    public StringProperty getTextProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }
}
