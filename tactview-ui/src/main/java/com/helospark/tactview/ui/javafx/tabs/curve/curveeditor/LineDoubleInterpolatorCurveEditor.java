package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.numerical.line.LineDoubleInterpolator;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

@Component
public class LineDoubleInterpolatorCurveEditor extends AbstractNoOpCurveEditor {

    @Override
    public void initializeControl(ControlInitializationRequest request) {
        LineDoubleInterpolator randomDoubleInterpolator = (LineDoubleInterpolator) request.effectInterpolator;

        createFieldFieldFor(request, "Tangent", 0, () -> randomDoubleInterpolator.getTangent(), value -> randomDoubleInterpolator.setTangent(value));
        createFieldFieldFor(request, "Start value", 1, () -> randomDoubleInterpolator.getStartValue(), value -> randomDoubleInterpolator.setStartValue(value));
    }

    private void createFieldFieldFor(ControlInitializationRequest request, String label, int index, Supplier<BigDecimal> getter, Consumer<BigDecimal> setter) {
        request.gridToInitialize.add(new Label(label), 0, index);
        TextField textField = new TextField(String.valueOf(getter.get()));
        changeIfRequested(request, setter, textField);
        request.gridToInitialize.add(textField, 1, index);
    }

    private void changeIfRequested(ControlInitializationRequest request, Consumer<BigDecimal> consumer, TextField textField) {
        textField.textProperty()
                .addListener(a -> {
                    try {
                        BigDecimal newValue = new BigDecimal(textField.getText());
                        consumer.accept(newValue);
                        request.updateRunnable.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public boolean supports(EffectInterpolator interpolator) {
        return interpolator instanceof LineDoubleInterpolator;
    }

}
