package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import javafx.scene.layout.GridPane;

public abstract class AbstractNoOpCurveEditor implements CurveEditor {

    @Override
    public void initializeControl(GridPane controlPane) {

    }

    @Override
    public boolean onMouseMoved(CurveEditorMouseRequest mouseEvent) {
        return true;
    }

    @Override
    public boolean onMouseDragged(CurveEditorMouseRequest mouseEvent) {
        return false;
    }

    @Override
    public boolean onMouseClicked(CurveEditorMouseRequest request) {
        return false;
    }

}
