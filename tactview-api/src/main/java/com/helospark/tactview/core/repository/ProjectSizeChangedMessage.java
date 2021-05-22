package com.helospark.tactview.core.repository;

public class ProjectSizeChangedMessage {
    ProjectSizeChangeType type;

    public ProjectSizeChangedMessage(ProjectSizeChangeType type) {
        this.type = type;
    }

    public ProjectSizeChangeType getType() {
        return type;
    }

    public static enum ProjectSizeChangeType {
        CLEARED,
        AUDIO,
        VIDEO
    }
}
