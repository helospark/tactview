package com.helospark.tactview.ui.javafx.control;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

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

        widthField.textProperty().addListener(a -> {
            int actualWidth = widthField.getValue();
            int expectedHeight = (int) (actualWidth / originalAspectRatio);

            if (isChainedProperty.get() && heightField.getValue() != expectedHeight) {
                heightField.setValue(expectedHeight);
            }
        });
        heightField.textProperty().addListener(a -> {
            int actualHeight = heightField.getValue();
            int expectedWidth = (int) (actualHeight * originalAspectRatio);

            if (isChainedProperty.get() && widthField.getValue() != expectedWidth) {
                widthField.setValue(expectedWidth);
            }
        });

        this.getChildren().addAll(widthField, linkResolutionButton, heightField);
        this.getStyleClass().add("resolution-component");
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
