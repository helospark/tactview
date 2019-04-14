package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;

import javafx.scene.canvas.Canvas;

public abstract class AbstractCurveEditorRequest {
    public KeyframeableEffect currentProvider;
    public DoubleInterpolator currentKeyframeableEffect;
    public double curveViewerOffsetSeconds;
    public double secondsPerPixel;
    public double minValue;
    public double maxValue;
    public double displayScale;
    public double height;
    public Canvas canvas;
}
