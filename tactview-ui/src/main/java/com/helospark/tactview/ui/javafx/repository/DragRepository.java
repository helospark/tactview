package com.helospark.tactview.ui.javafx.repository;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;
import com.helospark.tactview.ui.javafx.uicomponents.EffectDragInformation;

@Component
public class DragRepository implements CleanableMode {
    private ClipDragInformation clipDragInformation;
    private EffectDragInformation effectDragInformation;
    private SelectionBoxInformation selectionBoxInformation;
    private boolean isResizing;
    private DragDirection dragDirection;

    private double initialX = -1;
    private String initialId = "";

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
        initialX = -1;
        initialId = "";
    }

    public void onEffectDragged(EffectDragInformation effectDragInformation) {
        this.effectDragInformation = effectDragInformation;
    }

    public EffectDragInformation currentEffectDragInformation() {
        return effectDragInformation;
    }

    public void clearEffectDrag() {
        effectDragInformation = null;
        isResizing = false;
        dragDirection = null;
        initialX = -1;
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

    public double getInitialX() {
        return initialX;
    }

    public String getInitialId() {
        return initialId;
    }

    public void setInitialX(double initialX, String initialId) {
        this.initialX = initialX;
        this.initialId = initialId;
    }

    public void onEffectResized(EffectDragInformation dragInformation, DragDirection dragDirection) {
        this.effectDragInformation = dragInformation;
        this.isResizing = true;
        this.dragDirection = dragDirection;
    }

    public void onBoxSelectStarted(Point point) {
        selectionBoxInformation = new SelectionBoxInformation(point);
    }

    public void onBoxSelectEnded() {
        selectionBoxInformation = null;
    }

    public boolean isBoxSelectInProgress() {
        return selectionBoxInformation != null;
    }

    public SelectionBoxInformation getSelectionBoxInformation() {
        return selectionBoxInformation;
    }

    public static enum DragDirection {
        LEFT,
        RIGHT
    }

    @Override
    public void clean() {
        clearClipDrag();
        clearEffectDrag();
        onBoxSelectEnded();
    }

}
