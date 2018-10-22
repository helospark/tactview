package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

@Component
public class IntegerPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<IntegerProvider> {
    private static final int SMALL_RANGE = 1000;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;

    public IntegerPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter) {
        super(IntegerProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
    }

    @Override
    protected EffectLine handle(IntegerProvider integerProvider) {
        if (integerProvider.getMax() - integerProvider.getMin() < SMALL_RANGE) {
            return createSliderForSmallRange(integerProvider);
        } else {
            return createInputFieldForLargeRange(integerProvider);
        }
    }

    private EffectLine createSliderForSmallRange(IntegerProvider integerProvider) {
        Slider slider = new Slider();
        slider.setMin(integerProvider.getMin());
        slider.setMax(integerProvider.getMax());
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setValue(integerProvider.getValueAt(TimelinePosition.ofZero()));
        return PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> doubleToString(slider.getValue(), integerProvider))
                .withDescriptorId(integerProvider.getId())
                .withUpdateFunction(position -> slider.setValue(integerProvider.getValueAt(position)))
                .withVisibleNode(slider)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();

    }

    private EffectLine createInputFieldForLargeRange(IntegerProvider integerProvider) {
        TextField textField = new TextField();
        return PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textField.getText())
                .withDescriptorId(integerProvider.getId())
                .withUpdateFunction(position -> textField.setText(integerProviderValueToString(integerProvider, position)))
                .withVisibleNode(textField)
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .build();
    }

    private String doubleToString(double value, KeyframeableEffect integerProvider) {
        return String.valueOf(value);
    }

    private String integerProviderValueToString(IntegerProvider integerProvider, TimelinePosition position) {
        return Integer.toString((integerProvider.getValueAt(position)));
    }

}
