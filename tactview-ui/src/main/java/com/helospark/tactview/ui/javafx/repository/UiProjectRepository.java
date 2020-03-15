package com.helospark.tactview.ui.javafx.repository;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveLoadContributor;

import javafx.beans.property.SimpleIntegerProperty;

@Component
@Order(value = -1)
public class UiProjectRepository implements SaveLoadContributor, ResettableBean {
    private static final int PREVIEW_WIDTH = 320;
    private static final int PREVIEW_HEIGHT = 200;

    private double scaleFactor = 1.0;
    private SimpleIntegerProperty previewWidth = new SimpleIntegerProperty(PREVIEW_WIDTH);
    private SimpleIntegerProperty previewHeight = new SimpleIntegerProperty(PREVIEW_HEIGHT);
    private double aspectRatio;

    @JsonIgnore
    private ObjectMapper objectMapper;

    public UiProjectRepository(@Qualifier("getterIgnoringObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        resetDefaults();
    }

    @Override
    public void resetDefaults() {
        scaleFactor = 1.0;
        previewWidth.set(PREVIEW_WIDTH);
        previewHeight.set(PREVIEW_HEIGHT);
    }

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

    @Override
    public void generateSavedContent(Map<String, Object> generatedContent) {
        Map<String, Object> data = new HashMap<>();
        data.put("scaleFactor", scaleFactor);
        data.put("previewWidth", previewWidth.get());
        data.put("previewHeight", previewHeight.get());
        data.put("aspectRatio", aspectRatio);

        generatedContent.put("uiProjectRepository", data);
    }

    @Override
    public void loadFrom(JsonNode tree, LoadMetadata metadata) {
        JsonNode node = tree.get("uiProjectRepository");
        this.scaleFactor = node.get("scaleFactor").doubleValue();
        this.previewWidth.set(node.get("previewWidth").intValue());
        this.previewHeight.set(node.get("previewHeight").intValue());
        this.aspectRatio = node.get("aspectRatio").doubleValue();
    }

}
