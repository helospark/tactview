package com.helospark.tactview.ui.javafx.uicomponents.pattern;

public class TimelinePatternChangedMessage {
    private String componentId;
    private ChangeType changeType;

    public TimelinePatternChangedMessage(String componentId, ChangeType changeType) {
        this.componentId = componentId;
        this.changeType = changeType;
    }

    public String getComponentId() {
        return componentId;
    }

    public static enum ChangeType {
        REMOVED,
        ADDED
    }

}
