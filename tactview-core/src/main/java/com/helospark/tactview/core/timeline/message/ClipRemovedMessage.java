package com.helospark.tactview.core.timeline.message;

public class ClipRemovedMessage {
    private String elementId;

    public ClipRemovedMessage(String elementId) {
        this.elementId = elementId;
    }

    public String getElementId() {
        return elementId;
    }

    @Override
    public String toString() {
        return "ClipRemovedMessage [elementId=" + elementId + "]";
    }

}
