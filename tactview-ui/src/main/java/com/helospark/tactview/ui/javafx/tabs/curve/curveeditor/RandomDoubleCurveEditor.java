package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.math.BigDecimal;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl.RandomDoubleInterpolator;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

@Component
public class RandomDoubleCurveEditor extends AbstractNoOpCurveEditor {

    @Override
    public void initializeControl(ControlInitializationRequest request) {
        GridPane controlPane = request.gridToInitialize;
        controlPane.add(new Label("Frequency"), 0, 0);

        RandomDoubleInterpolator randomDoubleInterpolator = (RandomDoubleInterpolator) request.effectInterpolator;
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
