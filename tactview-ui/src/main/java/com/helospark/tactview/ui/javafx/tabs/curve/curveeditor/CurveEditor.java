package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

public interface CurveEditor {

    public void initializeControl(ControlInitializationRequest request);

    public boolean supports(EffectInterpolator interpolator);

    public boolean onMouseUp(CurveEditorMouseRequest mouseEvent);

    public boolean onMouseDown(CurveEditorMouseRequest mouseEvent);

    public boolean onMouseMoved(CurveEditorMouseRequest mouseEvent);

    public boolean onMouseDragged(CurveEditorMouseRequest mouseEvent);

    public boolean onMouseClicked(CurveEditorMouseRequest request);

    public void drawAdditionalUi(CurveDrawRequest drawRequest);

}
