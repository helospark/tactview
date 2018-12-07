package com.helospark.tactview.ui.javafx.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;

public class ColorWheelPicker extends Control {
    private ObjectProperty<Color> color;
    private ObjectProperty<Color> onActionProvider;

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ColorWheelPickerSkin(this);
    }

    public ObjectProperty<Color> colorProperty() {
        if (color == null) {
            color = new SimpleObjectProperty<>(this, "color", new Color(0.0, 0.0, 0.0, 1.0));
            color.set(new Color(0.0, 0.0, 0.0, 1.0));
        }
        return color;
    }

    public ObjectProperty<Color> onActionProperty() {
        if (onActionProvider == null) {
            onActionProvider = new SimpleObjectProperty<>(this, "action", new Color(1.0, 1.0, 1.0, 1.0));
        }
        return onActionProvider;
    }

    public void setValue(Color colorToSet) {
        colorProperty().set(colorToSet);
    }

}
