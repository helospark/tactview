package com.helospark.tactview.ui.javafx.repository;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;

@Component
public class DragRepository {
    private ClipDragInformation clipDragInformation;

    public void onClipDragged(ClipDragInformation information) {
        this.clipDragInformation = information;
    }

    public ClipDragInformation currentlyDraggedEffect() {
        return clipDragInformation;
    }

    public void clearDrag() {
        clipDragInformation = null;
    }
}
