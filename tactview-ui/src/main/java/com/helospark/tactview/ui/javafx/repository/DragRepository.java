package com.helospark.tactview.ui.javafx.repository;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;
import com.helospark.tactview.ui.javafx.uicomponents.EffectDragInformation;

@Component
public class DragRepository {
    private ClipDragInformation clipDragInformation;
    private EffectDragInformation effectDragInformation;
    private boolean isResizing;
    private DragDirection dragDirection;

    public void onClipDragged(ClipDragInformation information) {
        this.clipDragInformation = information;
        this.isResizing = false;
    }

    public void onClipResizing(ClipDragInformation information, DragDirection dragDirection) {
        this.clipDragInformation = information;
        this.isResizing = true;
        this.dragDirection = dragDirection;
    }

    public ClipDragInformation currentlyDraggedClip() {
        return clipDragInformation;
    }

    public void clearClipDrag() {
        clipDragInformation = null;
        isResizing = false;
        dragDirection = null;
    }

    public void onEffectDragged(EffectDragInformation effectDragInformation) {
        this.effectDragInformation = effectDragInformation;
    }

    public EffectDragInformation currentEffectDragInformation() {
        return effectDragInformation;
    }

    public void clearEffectDrag() {
        effectDragInformation = null;
    }

    public boolean isResizing() {
        return isResizing;
    }

    public ClipDragInformation getClipDragInformation() {
        return clipDragInformation;
    }

    public DragDirection getDragDirection() {
        return dragDirection;
    }

    public void onEffectResized(EffectDragInformation dragInformation, DragDirection dragDirection) {
        this.effectDragInformation = dragInformation;
        this.isResizing = true;
        this.dragDirection = dragDirection;
    }

    public static enum DragDirection {
        LEFT,
        RIGHT
    }

}
