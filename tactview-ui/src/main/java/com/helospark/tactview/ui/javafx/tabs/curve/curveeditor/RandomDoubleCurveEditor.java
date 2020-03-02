package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.math.BigDecimal;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl.RandomDoubleInterpolator;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

@Component
public class RandomDoubleCurveEditor extends EditableFieldSupportingCurveEditor {

    public RandomDoubleCurveEditor(UiCommandInterpreterService commandInterpreterService) {
        super(commandInterpreterService);
    }

    @Override
    public void initializeControl(ControlInitializationRequest request) {
        GridPane controlPane = request.gridToInitialize;
        RandomDoubleInterpolator randomDoubleInterpolator = (RandomDoubleInterpolator) request.effectInterpolator;

        createFieldFieldFor(request, "Frequency", 0, () -> randomDoubleInterpolator.getChangeScale().doubleValue(), value -> randomDoubleInterpolator.setChangeScale(BigDecimal.valueOf(value))); // TODO: add native support for other types
        createFieldFieldFor(request, "Min", 1, () -> randomDoubleInterpolator.getMin(), value -> randomDoubleInterpolator.setMin(value));
        createFieldFieldFor(request, "Max", 2, () -> randomDoubleInterpolator.getMax(), value -> randomDoubleInterpolator.setMax(value));
        createFieldFieldFor(request, "Seed", 3, () -> (double) randomDoubleInterpolator.getSeed(), value -> randomDoubleInterpolator.setSeed(value.intValue())); // TODO: add native support for other types

        TextField textField = new TextField(randomDoubleInterpolator.getChangeScale().toString());
        textField.textProperty()
                .addListener(a -> {
                    try {
                        BigDecimal newValue = new BigDecimal(textField.getText());
                        if (newValue.compareTo(BigDecimal.ZERO) > 0) {
                            randomDoubleInterpolator.changeScale(newValue);
                            request.updateRunnable.run();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        controlPane.add(textField, 1, 0);
    }

    @Override
    public boolean supports(EffectInterpolator interpolator) {
        return interpolator instanceof RandomDoubleInterpolator;
    }

}
