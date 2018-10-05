package com.helospark.tactview.ui.javafx.repository;

import com.helospark.lightdi.annotation.Component;

import javafx.beans.property.SimpleIntegerProperty;

@Component
public class UiProjectRepository {
    private static final int PREVIEW_WIDTH = 320;
    private static final int PREVIEW_HEIGHT = 200;
    private double scaleFactor = 1.0;
    private SimpleIntegerProperty previewWidth = new SimpleIntegerProperty(PREVIEW_WIDTH);
    private SimpleIntegerProperty previewHeight = new SimpleIntegerProperty(PREVIEW_HEIGHT);
    private double aspectRatio;

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public SimpleIntegerProperty getPreviewWidthProperty() {
        return previewWidth;
    }

    public SimpleIntegerProperty getPreviewHeightProperty() {
        return previewHeight;
    }

    public int getPreviewWidth() {
        return previewWidth.get();
    }

    public int getPreviewHeight() {
        return previewHeight.get();
    }

    public void setPreviewWidth(int newValue) {
        this.previewWidth.set(newValue);
    }

    public void setPreviewHeight(int newValue) {
        this.previewHeight.set(newValue);
    }

    public void setAspectRatio(double newValue) {
        this.aspectRatio = newValue;
    }

}
