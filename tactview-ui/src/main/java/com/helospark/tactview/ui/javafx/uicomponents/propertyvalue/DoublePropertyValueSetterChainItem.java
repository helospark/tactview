package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

import javafx.scene.control.TextField;

@Component
public class DoublePropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<DoubleProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;

    public DoublePropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter) {
        super(DoubleProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
    }

    @Override
    protected EffectLine handle(DoubleProvider doubleProvider) {
        TextField textField = new TextField();
        return PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textField.getText())
                .withDescriptorId(doubleProvider.getId())
                .withUpdateFunction(position -> textField.setText(doubleProviderValueToString(doubleProvider, position)))
                .withUpdateFromValue(value -> textField.setText(String.valueOf(value)))
                .withVisibleNode(textField)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();

    }

    private String doubleProviderValueToString(DoubleProvider doubleProvider, TimelinePosition position) {
        return Double.toString(doubleProvider.getValueAt(position));
    }

}
