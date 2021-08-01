package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain;

public class TimelineUiCacheElement {
    public String elementId;
    public TimelineUiCacheType elementType;
    public CollisionRectangle rectangle;

    public TimelineUiCacheElement(String elementId, TimelineUiCacheType elementType, CollisionRectangle rectangle) {
        this.elementId = elementId;
        this.elementType = elementType;
        this.rectangle = rectangle;
    }

}
