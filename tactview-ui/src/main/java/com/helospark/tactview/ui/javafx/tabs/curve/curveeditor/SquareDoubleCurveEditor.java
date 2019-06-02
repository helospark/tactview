package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.square.SquareDoubleInterpolator;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

@Component
public class SquareDoubleCurveEditor extends EditableFieldSupportingCurveEditor {

    public SquareDoubleCurveEditor(UiCommandInterpreterService commandInterpreterService) {
        super(commandInterpreterService);
    }

    @Override
    public void initializeControl(ControlInitializationRequest request) {
        SquareDoubleInterpolator squareDoubleInterpolator = (SquareDoubleInterpolator) request.effectInterpolator;

        createFieldFieldFor(request, "On time", 0, () -> squareDoubleInterpolator.getOnTime(), value -> squareDoubleInterpolator.setOnTime(value));
        createFieldFieldFor(request, "Off time", 1, () -> squareDoubleInterpolator.getOffTime(), value -> squareDoubleInterpolator.setOffTime(value));
        createFieldFieldFor(request, "Min value", 2, () -> squareDoubleInterpolator.getMinValue(), value -> squareDoubleInterpolator.setMinValue(value));
        createFieldFieldFor(request, "Max value", 3, () -> squareDoubleInterpolator.getMaxValue(), value -> squareDoubleInterpolator.setMaxValue(value));
    }

    @Override
    public boolean supports(EffectInterpolator interpolator) {
        return interpolator instanceof SquareDoubleInterpolator;
    }

}
