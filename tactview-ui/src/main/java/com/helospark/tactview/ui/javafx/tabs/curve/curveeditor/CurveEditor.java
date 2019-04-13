package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

import javafx.scene.layout.GridPane;

public interface CurveEditor {

    public void initializeControl(GridPane controlPane);

    public boolean supports(EffectInterpolator interpolator);

    public boolean onMouseMoved(CurveEditorMouseRequest mouseEvent);

    public boolean onMouseDragged(CurveEditorMouseRequest mouseEvent);

    public boolean onMouseClicked(CurveEditorMouseRequest request);

}
