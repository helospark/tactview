package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import static java.lang.Integer.MAX_VALUE;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

@Component
@Order(MAX_VALUE)
public class OnlyDisplayingCurveEditor extends AbstractNoOpCurveEditor {

    @Override
    public boolean supports(EffectInterpolator interpolator) {
        return true;
    }

}
