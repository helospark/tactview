package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain;

public class UiTimelineChange {
    UiTimelineChangeType type;

    public UiTimelineChange(UiTimelineChangeType type) {
        this.type = type;
    }

    public UiTimelineChangeType getType() {
        return type;
    }

}
