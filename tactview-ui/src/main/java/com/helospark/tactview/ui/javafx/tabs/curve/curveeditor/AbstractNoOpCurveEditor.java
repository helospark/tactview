package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.math.BigDecimal;

import com.helospark.lightdi.annotation.Autowired;
import com.helospark.tactview.ui.javafx.UiTimelineManager;

public abstract class AbstractNoOpCurveEditor implements CurveEditor {
    private UiTimelineManager uiTimelineManager;

    @Override
    public void initializeControl(ControlInitializationRequest request) {

    }

    @Override
    public boolean onMouseMoved(CurveEditorMouseRequest mouseEvent) {
        return true;
    }

    @Override
    public boolean onMouseUp(CurveEditorMouseRequest mouseEvent) {
        return false;
    }

    @Override
    public boolean onMouseDown(CurveEditorMouseRequest mouseEvent) {
        return false;
    }

    @Override
    public boolean onMouseDragged(CurveEditorMouseRequest mouseEvent) {
        jumpToPositionOnDrag(mouseEvent, true);
        return false;
    }

    @Override
    public boolean onMouseClicked(CurveEditorMouseRequest request) {
        return false;
    }

    @Override
    public void drawAdditionalUi(CurveDrawRequest drawRequest) {

    }

    @Override
    public boolean onMouseDragEnded(CurveEditorMouseRequest mouseEvent) {
        return false;
    }

    protected void jumpToPositionOnDrag(CurveEditorMouseRequest mouseEvent, boolean isEmptyPointDragged) {
        if (isEmptyPointDragged) {
            uiTimelineManager.jumpAbsolute(BigDecimal.valueOf(mouseEvent.remappedMousePosition.x));
        }
    }

    @Autowired
    public void setUiTimelineManager(UiTimelineManager uiTimelineManager) {
        this.uiTimelineManager = uiTimelineManager;
    }
}
