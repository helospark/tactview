package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

import javafx.scene.input.MouseEvent;

public class CurveEditorMouseRequest {
    public MouseEvent event;
    public EffectInterpolator currentKeyframeableEffect;

    public CurveEditorMouseRequest(MouseEvent event, EffectInterpolator currentKeyframeableEffect) {
        this.event = event;
        this.currentKeyframeableEffect = currentKeyframeableEffect;
    }

}
