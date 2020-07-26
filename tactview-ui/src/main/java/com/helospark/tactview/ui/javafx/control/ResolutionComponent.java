package com.helospark.tactview.ui.javafx.control;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class ResolutionComponent extends HBox {
    private NumericalTextField widthField;
    private NumericalTextField heightField;
    private Button linkResolutionButton;
    private SimpleBooleanProperty isChainedProperty = new SimpleBooleanProperty(true);

    private double originalAspectRatio = 0.0;

    private final InvalidationListener HEIGHT_CHANGE_LISTENER = createHeightChangeListener();
    private final InvalidationListener WIDTH_CHANGE_LISTENER = createWidthChangeListener();

    public ResolutionComponent(int width, int height) {
        originalAspectRatio = (double) width / height;

        widthField = new NumericalTextField(width);
        widthField.getStyleClass().add("resolution-width-field");

        heightField = new NumericalTextField(height);
        heightField.getStyleClass().add("resolution-width-field");

        linkResolutionButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.CHAIN));
        linkResolutionButton.getStyleClass().add("resolution-link-button");

        isChainedProperty.addListener(e -> {
            FontAwesome.Glyph icon;
            if (isChainedProperty.get()) {
                icon = FontAwesome.Glyph.CHAIN;
            } else {
                icon = FontAwesome.Glyph.CHAIN_BROKEN;
            }
            linkResolutionButton.graphicProperty().set(new Glyph("FontAwesome", icon));
        });
        isChainedProperty.set(true);

        linkResolutionButton.setOnAction(e -> {
            isChainedProperty.set(!isChainedProperty.get());
        });
        linkResolutionButton.setTooltip(new Tooltip("Link width to height"));

        widthField.textProperty().addListener(WIDTH_CHANGE_LISTENER);
        heightField.textProperty().addListener(HEIGHT_CHANGE_LISTENER);

        this.getChildren().addAll(widthField, linkResolutionButton, heightField);
        this.getStyleClass().add("resolution-component");
    }

    protected InvalidationListener createHeightChangeListener() {
        return a -> {
            int actualHeight = heightField.getValue();
            int expectedWidth = (int) (actualHeight * originalAspectRatio);

            if (isChainedProperty.get() && widthField.getValue() != expectedWidth) {
                widthField.textProperty().removeListener(WIDTH_CHANGE_LISTENER);
                widthField.setValue(expectedWidth);
                widthField.textProperty().addListener(WIDTH_CHANGE_LISTENER);
            }
        };
    }

    protected InvalidationListener createWidthChangeListener() {
        return a -> {
            int actualWidth = widthField.getValue();
            int expectedHeight = (int) (actualWidth / originalAspectRatio);

            if (isChainedProperty.get() && heightField.getValue() != expectedHeight) {
                heightField.textProperty().removeListener(HEIGHT_CHANGE_LISTENER);
                heightField.setValue(expectedHeight);
                heightField.textProperty().addListener(HEIGHT_CHANGE_LISTENER);
            }
        };
    }

    public void setLinkWidthHeightConnection(boolean link) {
        isChainedProperty.set(link);
    }

    public int getResolutionWidth() {
        return widthField.getValue();
    }

    public int getResolutionHeight() {
        return heightField.getValue();
    }

}
