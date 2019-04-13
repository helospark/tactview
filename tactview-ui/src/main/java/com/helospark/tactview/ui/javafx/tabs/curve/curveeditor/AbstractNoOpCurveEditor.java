package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

public abstract class AbstractNoOpCurveEditor implements CurveEditor {

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
        return false;
    }

    @Override
    public boolean onMouseClicked(CurveEditorMouseRequest request) {
        return false;
    }

    @Override
    public void drawAdditionalUi(CurveDrawRequest drawRequest) {

    }

}
