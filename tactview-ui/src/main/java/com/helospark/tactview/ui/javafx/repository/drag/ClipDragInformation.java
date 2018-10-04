package com.helospark.tactview.ui.javafx.repository.drag;

import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.scene.Group;

public class ClipDragInformation {
    private Group node;
    private TimelinePosition originalPosition;
    private String clipId;
    private String originalChannelId;

    public ClipDragInformation(Group node, TimelinePosition originalPosition, String clipId, String originalChannelId) {
        this.node = node;
        this.originalPosition = originalPosition;
        this.clipId = clipId;
        this.originalChannelId = originalChannelId;
    }

    public Group getNode() {
        return node;
    }

    public TimelinePosition getOriginalPosition() {
        return originalPosition;
    }

    public String getClipId() {
        return clipId;
    }

    public String getOriginalChannelId() {
        return originalChannelId;
    }

}
