package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;

public abstract class AbstractCurveEditorRequest {
    public DoubleInterpolator currentKeyframeableEffect;
    public double curveViewerOffsetSeconds;
    public double secondsPerPixel;
    public double minValue;
    public double maxValue;
    public double displayScale;
}
