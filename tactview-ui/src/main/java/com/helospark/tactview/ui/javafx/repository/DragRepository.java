package com.helospark.tactview.ui.javafx.repository;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;
import com.helospark.tactview.ui.javafx.uicomponents.EffectDragInformation;

@Component
public class DragRepository {
    private ClipDragInformation clipDragInformation;
    private EffectDragInformation effectDragInformation;

    public void onClipDragged(ClipDragInformation information) {
        this.clipDragInformation = information;
    }

    public ClipDragInformation currentlyDraggedClip() {
        return clipDragInformation;
    }

    public void clearClipDrag() {
        clipDragInformation = null;
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

}
