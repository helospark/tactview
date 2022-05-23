package com.helospark.tactview.ui.javafx.uicomponents.window.projectmedia;

import java.util.List;

import com.helospark.tactview.core.timeline.TimelineClip;

import javafx.scene.image.Image;

public class ProjectMediaElement {
    String id;
    String label;
    List<TimelineClip> templateClips;

    // UI
    Image defaultImage;
    List<Image> previewImages = List.of();

    public ProjectMediaElement(String id, List<TimelineClip> templateClips, Image defaultImage, String label) {
        this.id = id;
        this.templateClips = templateClips;
        this.defaultImage = defaultImage;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public List<TimelineClip> getTemplateClips() {
        return templateClips;
    }

    public Image getDefaultImage() {
        return defaultImage;
    }

    public List<Image> getPreviewImages() {
        return previewImages;
    }

    public void setPreviewImages(List<Image> images) {
        this.previewImages = images;
    }

}
