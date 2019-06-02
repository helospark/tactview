package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.sine.SineDoubleInterpolator;

@Component
public class SineDoubleCurveEditor extends EditableFieldSupportingCurveEditor {

    @Override
    public void initializeControl(ControlInitializationRequest request) {
        SineDoubleInterpolator randomDoubleInterpolator = (SineDoubleInterpolator) request.effectInterpolator;

        createFieldFieldFor(request, "Frequency", 0, () -> randomDoubleInterpolator.getFrequency(), value -> randomDoubleInterpolator.setFrequency(value));
        createFieldFieldFor(request, "Min value", 1, () -> randomDoubleInterpolator.getMinValue(), value -> randomDoubleInterpolator.setMinValue(value));
        createFieldFieldFor(request, "Max value", 2, () -> randomDoubleInterpolator.getMaxValue(), value -> randomDoubleInterpolator.setMaxValue(value));
        createFieldFieldFor(request, "Start offset", 3, () -> randomDoubleInterpolator.getStartOffset(), value -> randomDoubleInterpolator.setStartOffset(value));
    }

    @Override
    public boolean supports(EffectInterpolator interpolator) {
        return interpolator instanceof SineDoubleInterpolator;
    }

}
