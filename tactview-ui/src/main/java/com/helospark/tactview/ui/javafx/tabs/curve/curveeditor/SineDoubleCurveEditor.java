package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.sine.SineDoubleInterpolator;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

@Component
public class SineDoubleCurveEditor extends AbstractNoOpCurveEditor {

    @Override
    public void initializeControl(ControlInitializationRequest request) {
        SineDoubleInterpolator randomDoubleInterpolator = (SineDoubleInterpolator) request.effectInterpolator;

        createFieldFieldFor(request, "Frequency", 0, () -> randomDoubleInterpolator.getFrequency(), value -> randomDoubleInterpolator.setFrequency(value));
        createFieldFieldFor(request, "Min value", 1, () -> randomDoubleInterpolator.getMinValue(), value -> randomDoubleInterpolator.setMinValue(value));
        createFieldFieldFor(request, "Max value", 2, () -> randomDoubleInterpolator.getMaxValue(), value -> randomDoubleInterpolator.setMaxValue(value));
        createFieldFieldFor(request, "Start offset", 3, () -> randomDoubleInterpolator.getStartOffset(), value -> randomDoubleInterpolator.setStartOffset(value));
    }

    private void createFieldFieldFor(ControlInitializationRequest request, String label, int index, Supplier<Double> getter, Consumer<Double> setter) {
        request.gridToInitialize.add(new Label(label), 0, index);
        TextField textField = new TextField(String.valueOf(getter.get()));
        changeIfRequested(request, setter, textField);
        request.gridToInitialize.add(textField, 1, index);
    }

    private void changeIfRequested(ControlInitializationRequest request, Consumer<Double> consumer, TextField textField) {
        textField.textProperty()
                .addListener(a -> {
                    try {
                        BigDecimal newValue = new BigDecimal(textField.getText());
                        consumer.accept(newValue.doubleValue());
                        request.updateRunnable.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public boolean supports(EffectInterpolator interpolator) {
        return interpolator instanceof SineDoubleInterpolator;
    }

}
