package com.helospark.tactview.ui.javafx.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;

public class ColorWheelPicker extends Control {
    private ObjectProperty<Color> color;
    private ObjectProperty<Color> onActionProvider;
    private BooleanProperty onValueChaning;

    private Color startColor = Color.BLACK;

    private boolean listenersDisabled = false;

    public ColorWheelPicker() {
        onValueChangingProperty().addListener((a, oldValue, newValue) -> {
            if (!oldValue && newValue) {
                System.out.println("Setting startColor " + color.get());
                startColor = color.get();
            }
        });
    }

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
            onActionProvider = new SimpleObjectProperty<>(this, "action", new Color(0.0, 0.0, 0.0, 1.0));
        }
        return onActionProvider;
    }

    public BooleanProperty onValueChangingProperty() {
        if (onValueChaning == null) {
            onValueChaning = new SimpleBooleanProperty(this, "valueChanging", false);
        }
        return onValueChaning;
    }

    public Color getColorChangeStart() {
        return startColor;
    }

    public void setValue(Color colorToSet) {
        listenersDisabled = true;
        colorProperty().set(colorToSet);
        onActionProperty().set(colorToSet);
        listenersDisabled = false;
    }

    public boolean isListenersDisabled() {
        return listenersDisabled;
    }

}
