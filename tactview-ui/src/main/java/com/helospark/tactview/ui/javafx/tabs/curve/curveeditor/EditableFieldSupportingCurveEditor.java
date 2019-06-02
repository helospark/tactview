package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.GenericLambdaExecutorCommand;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public abstract class EditableFieldSupportingCurveEditor extends AbstractNoOpCurveEditor {
    private UiCommandInterpreterService commandInterpreterService;

    public EditableFieldSupportingCurveEditor(UiCommandInterpreterService commandInterpreterService) {
        this.commandInterpreterService = commandInterpreterService;
    }

    protected void createFieldFieldFor(ControlInitializationRequest request, String label, int index, Supplier<Double> getter, Consumer<Double> setter) {
        request.gridToInitialize.add(new Label(label), 0, index);
        TextField textField = new TextField(String.valueOf(getter.get()));

        Consumer<Double> updateRunnable = newValue -> {
            String newTextToSet = String.valueOf(newValue);
            if (!textField.getText().equals(newTextToSet) && !textField.isFocused()) {
                textField.setText(newTextToSet);
            }
            request.updateRunnable.run();
        };

        changeIfRequested(updateRunnable, setter, getter, textField);
        request.gridToInitialize.add(textField, 1, index);
    }

    protected void changeIfRequested(Consumer<Double> updateRunnable, Consumer<Double> consumer, Supplier<Double> getter, TextField textField) {
        textField.focusedProperty()
                .addListener((event, oldEventValue, newEventValue) -> {
                    if (newEventValue == false) {
                        try {
                            BigDecimal newValue = new BigDecimal(textField.getText());

                            double currentValue = getter.get();

                            GenericLambdaExecutorCommand command = GenericLambdaExecutorCommand.builder()
                                    .withExecuteCommand(() -> {
                                        consumer.accept(newValue.doubleValue());
                                        updateRunnable.accept(newValue.doubleValue());
                                    })
                                    .withRevertCommand(() -> {
                                        consumer.accept(currentValue);
                                        updateRunnable.accept(currentValue);
                                    })
                                    .build();
                            commandInterpreterService.sendWithResult(command);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}
