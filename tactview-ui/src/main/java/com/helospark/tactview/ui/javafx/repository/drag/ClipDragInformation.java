package com.helospark.tactview.ui.javafx.repository.drag;

import javafx.scene.Group;

public class ClipDragInformation {
    private Group node;
    private int originalPosition;
    private String clipId;

    public ClipDragInformation(Group node, int originalPosition, String clipId) {
        this.node = node;
        this.originalPosition = originalPosition;
        this.clipId = clipId;
    }

    public Group getNode() {
        return node;
    }

    public int getOriginalPosition() {
        return originalPosition;
    }

    public String getClipId() {
        return clipId;
    }

}
