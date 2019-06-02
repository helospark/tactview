package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public abstract class EditableFieldSupportingCurveEditor extends AbstractNoOpCurveEditor {

    protected void createFieldFieldFor(ControlInitializationRequest request, String label, int index, Supplier<Double> getter, Consumer<Double> setter) {
        request.gridToInitialize.add(new Label(label), 0, index);
        TextField textField = new TextField(String.valueOf(getter.get()));
        changeIfRequested(request, setter, textField);
        request.gridToInitialize.add(textField, 1, index);
    }

    protected void changeIfRequested(ControlInitializationRequest request, Consumer<Double> consumer, TextField textField) {
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

}
